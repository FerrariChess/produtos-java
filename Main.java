import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    private static final Scanner ENTRADA = new Scanner(System.in);

    public static void main(String[] args) {
        ArrayList<Produto> produtos = new ArrayList<>();
        boolean sair = false;

        while (!sair) {
            exibirMenu();
            int opcao = lerInteiro("Escolha: ");

            switch (opcao) {
                case 1:
                    cadastrarProduto(produtos);
                    break;
                case 2:
                    listarProdutos(produtos);
                    break;
                case 3:
                    adicionarEstoque(produtos);
                    break;
                case 4:
                    editarProduto(produtos);
                    break;
                case 5:
                    removerProduto(produtos);
                    break;
                case 0:
                    sair = true;
                    System.out.println("Encerrando.");
                    break;
                default:
                    System.out.println("Opcao invalida.");
            }
        }
        ENTRADA.close();
    }

    private static void exibirMenu() {
        System.out.println();
        System.out.println("1 - Cadastrar produto");
        System.out.println("2 - Listar produtos");
        System.out.println("3 - Adicionar estoque");
        System.out.println("4 - Editar produto");
        System.out.println("5 - Remover produto");
        System.out.println("0 - Sair");
    }

    private static void cadastrarProduto(ArrayList<Produto> produtos) {
        try {
            System.out.print("Nome: ");
            String nome = ENTRADA.nextLine();
            double preco = lerDouble("Preco: ");
            int quantidade = lerInteiro("Quantidade: ");
            System.out.print("Categoria: ");
            String categoria = ENTRADA.nextLine();
            produtos.add(new Produto(nome, preco, quantidade, categoria));
            System.out.println("Produto cadastrado.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void listarProdutos(ArrayList<Produto> produtos) {
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        for (int i = 0; i < produtos.size(); i++) {
            System.out.println("[" + i + "] " + produtos.get(i));
        }
    }

    private static void adicionarEstoque(ArrayList<Produto> produtos) {
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        listarProdutos(produtos);
        int indice = lerInteiro("Indice do produto: ");
        if (indice < 0 || indice >= produtos.size()) {
            System.out.println("Indice invalido.");
            return;
        }
        int qtd = lerInteiro("Quantidade a adicionar: ");
        try {
            produtos.get(indice).adicionarEstoque(qtd);
            System.out.println("Estoque atualizado.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void editarProduto(ArrayList<Produto> produtos) {
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        listarProdutos(produtos);
        int indice = lerInteiro("Indice do produto a editar: ");
        if (indice < 0 || indice >= produtos.size()) {
            System.out.println("Indice invalido.");
            return;
        }
        Produto p = produtos.get(indice);
        try {
            System.out.print("Novo nome [" + p.getNome() + "]: ");
            String nome = ENTRADA.nextLine();
            if (!nome.isEmpty()) {
                p.setNome(nome);
            }
            String precoStr = lerLinhaOuVazio("Novo preco [" + p.getPreco() + "] (Enter para manter): ");
            if (!precoStr.isEmpty()) {
                p.setPreco(Double.parseDouble(precoStr.replace(',', '.')));
            }
            String qtdStr = lerLinhaOuVazio("Nova quantidade [" + p.getQuantidade() + "] (Enter para manter): ");
            if (!qtdStr.isEmpty()) {
                p.setQuantidade(Integer.parseInt(qtdStr.trim()));
            }
            System.out.print("Nova categoria [" + p.getCategoria() + "]: ");
            String categoria = ENTRADA.nextLine();
            if (!categoria.isEmpty()) {
                p.setCategoria(categoria);
            }
            System.out.println("Produto atualizado.");
        } catch (IllegalArgumentException e) {
            System.out.println("Erro: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Valor numerico invalido.");
        }
    }

    private static void removerProduto(ArrayList<Produto> produtos) {
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        listarProdutos(produtos);
        int indice = lerInteiro("Indice do produto a remover: ");
        if (indice < 0 || indice >= produtos.size()) {
            System.out.println("Indice invalido.");
            return;
        }
        produtos.remove(indice);
        System.out.println("Produto removido.");
    }

    private static int lerInteiro(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int v = ENTRADA.nextInt();
                ENTRADA.nextLine();
                return v;
            } catch (InputMismatchException e) {
                ENTRADA.nextLine();
                System.out.println("Digite um numero inteiro valido.");
            }
        }
    }

    private static double lerDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String linha = ENTRADA.nextLine().trim().replace(',', '.');
                return Double.parseDouble(linha);
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero valido.");
            }
        }
    }

    private static String lerLinhaOuVazio(String prompt) {
        System.out.print(prompt);
        return ENTRADA.nextLine();
    }
}
