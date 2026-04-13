import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API HTTP para testar o cadastro com Postman (ou curl).
 * Console interativo continua em {@link Main}.
 */
public class ServidorHttp {

    private static final int PORTA = 8080;
    private static final ArrayList<Produto> PRODUTOS = new ArrayList<>();

    private static final Pattern STR = Pattern.compile(
            "\"(nome|categoria)\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern NUM = Pattern.compile(
            "\"(preco|quantidade|qtd)\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");

    public static void main(String[] args) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(PORTA), 0);
        servidor.createContext("/api", new ApiHandler());
        servidor.setExecutor(null);
        servidor.start();
        System.out.println("API em http://127.0.0.1:" + PORTA + "/api");
        System.out.println("GET /api/produtos | GET /api/produtos/{id}");
        System.out.println("POST /api/produtos (JSON) | PUT /api/produtos/{id} (JSON)");
        System.out.println("DELETE /api/produtos/{id} | POST /api/produtos/{id}/estoque (JSON qtd)");
    }

    static final class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            cors(t);
            if ("OPTIONS".equalsIgnoreCase(t.getRequestMethod())) {
                t.sendResponseHeaders(204, -1);
                t.close();
                return;
            }

            String caminho = t.getRequestURI().getPath();
            if (!caminho.startsWith("/api")) {
                responder(t, 404, "{\"erro\":\"nao encontrado\"}");
                return;
            }
            String resto = caminho.substring("/api".length());
            if (resto.isEmpty()) {
                resto = "/";
            }
            if (!resto.startsWith("/")) {
                resto = "/" + resto;
            }

            String[] partes = resto.split("/");
            try {
                if (partes.length >= 2 && "produtos".equals(partes[1])) {
                    if (partes.length == 2) {
                        if ("GET".equals(t.getRequestMethod())) {
                            listar(t);
                            return;
                        }
                        if ("POST".equals(t.getRequestMethod())) {
                            criar(t);
                            return;
                        }
                    }
                    if (partes.length == 3) {
                        int id = Integer.parseInt(partes[2]);
                        if ("GET".equals(t.getRequestMethod())) {
                            obter(t, id);
                            return;
                        }
                        if ("PUT".equals(t.getRequestMethod())) {
                            substituir(t, id);
                            return;
                        }
                        if ("DELETE".equals(t.getRequestMethod())) {
                            remover(t, id);
                            return;
                        }
                    }
                    if (partes.length == 4 && "estoque".equals(partes[3])) {
                        int id = Integer.parseInt(partes[2]);
                        if ("POST".equals(t.getRequestMethod())) {
                            estoque(t, id);
                            return;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                responder(t, 400, "{\"erro\":\"id invalido\"}");
                return;
            }

            responder(t, 404, "{\"erro\":\"rota ou metodo nao suportado\"}");
        }
    }

    private static void cors(HttpExchange t) {
        t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static String corpo(HttpExchange t) throws IOException {
        return new String(t.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
    }

    private static void listar(HttpExchange t) throws IOException {
        synchronized (PRODUTOS) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < PRODUTOS.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(produtoJson(i, PRODUTOS.get(i)));
            }
            sb.append(']');
            responder(t, 200, sb.toString());
        }
    }

    private static void obter(HttpExchange t, int id) throws IOException {
        synchronized (PRODUTOS) {
            if (id < 0 || id >= PRODUTOS.size()) {
                responder(t, 404, "{\"erro\":\"produto nao encontrado\"}");
                return;
            }
            responder(t, 200, produtoJson(id, PRODUTOS.get(id)));
        }
    }

    private static void criar(HttpExchange t) throws IOException {
        String body = corpo(t);
        try {
            Campos c = parseCamposProduto(body, true);
            Produto p = new Produto(c.nome, c.preco, c.quantidade, c.categoria);
            int id;
            synchronized (PRODUTOS) {
                PRODUTOS.add(p);
                id = PRODUTOS.size() - 1;
            }
            responder(t, 201, produtoJson(id, p));
        } catch (IllegalArgumentException e) {
            responder(t, 400, "{\"erro\":" + escaparJson(e.getMessage()) + "}");
        }
    }

    private static void substituir(HttpExchange t, int id) throws IOException {
        String body = corpo(t);
        synchronized (PRODUTOS) {
            if (id < 0 || id >= PRODUTOS.size()) {
                responder(t, 404, "{\"erro\":\"produto nao encontrado\"}");
                return;
            }
            try {
                Campos c = parseCamposProduto(body, true);
                Produto p = PRODUTOS.get(id);
                p.setNome(c.nome);
                p.setPreco(c.preco);
                p.setQuantidade(c.quantidade);
                p.setCategoria(c.categoria);
                responder(t, 200, produtoJson(id, p));
            } catch (IllegalArgumentException e) {
                responder(t, 400, "{\"erro\":" + escaparJson(e.getMessage()) + "}");
            }
        }
    }

    private static void remover(HttpExchange t, int id) throws IOException {
        synchronized (PRODUTOS) {
            if (id < 0 || id >= PRODUTOS.size()) {
                responder(t, 404, "{\"erro\":\"produto nao encontrado\"}");
                return;
            }
            PRODUTOS.remove(id);
            responder(t, 200, "{\"ok\":true}");
        }
    }

    private static void estoque(HttpExchange t, int id) throws IOException {
        String body = corpo(t);
        synchronized (PRODUTOS) {
            if (id < 0 || id >= PRODUTOS.size()) {
                responder(t, 404, "{\"erro\":\"produto nao encontrado\"}");
                return;
            }
            int qtd = parseQtd(body);
            if (qtd < 0) {
                responder(t, 400, "{\"erro\":\"use campo qtd (inteiro >= 0) no JSON\"}");
                return;
            }
            try {
                PRODUTOS.get(id).adicionarEstoque(qtd);
                responder(t, 200, produtoJson(id, PRODUTOS.get(id)));
            } catch (IllegalArgumentException e) {
                responder(t, 400, "{\"erro\":" + escaparJson(e.getMessage()) + "}");
            }
        }
    }

    private static int parseQtd(String json) {
        Matcher m = NUM.matcher(json);
        while (m.find()) {
            if ("qtd".equals(m.group(1))) {
                double v = Double.parseDouble(m.group(2));
                return (int) Math.round(v);
            }
        }
        return -1;
    }

    private static final class Campos {
        String nome;
        double preco;
        int quantidade;
        String categoria;
    }

    /** Parse minimo de JSON para os campos do produto. */
    private static Campos parseCamposProduto(String json, boolean todosObrigatorios) {
        Campos c = new Campos();
        Matcher sm = STR.matcher(json);
        while (sm.find()) {
            if ("nome".equals(sm.group(1))) {
                c.nome = sm.group(2);
            } else if ("categoria".equals(sm.group(1))) {
                c.categoria = sm.group(2);
            }
        }
        Matcher nm = NUM.matcher(json);
        while (nm.find()) {
            if ("preco".equals(nm.group(1))) {
                c.preco = Double.parseDouble(nm.group(2));
            } else if ("quantidade".equals(nm.group(1))) {
                c.quantidade = (int) Math.round(Double.parseDouble(nm.group(2)));
            }
        }
        if (todosObrigatorios) {
            if (c.nome == null || c.categoria == null) {
                throw new IllegalArgumentException("JSON precisa de nome e categoria (strings).");
            }
            if (!json.contains("\"preco\"") || !json.contains("\"quantidade\"")) {
                throw new IllegalArgumentException("JSON precisa de preco e quantidade.");
            }
        }
        return c;
    }

    private static String produtoJson(int id, Produto p) {
        return "{"
                + "\"id\":" + id
                + ",\"nome\":" + escaparJson(p.getNome())
                + ",\"preco\":" + p.getPreco()
                + ",\"quantidade\":" + p.getQuantidade()
                + ",\"categoria\":" + escaparJson(p.getCategoria())
                + ",\"valorTotal\":" + String.format(java.util.Locale.US, "%.2f", p.calcularValorTotal())
                + "}";
    }

    private static String escaparJson(String s) {
        if (s == null) {
            return "\"\"";
        }
        String x = s.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + x + "\"";
    }

    private static void responder(HttpExchange t, int codigo, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        t.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        t.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(bytes);
        }
    }
}
