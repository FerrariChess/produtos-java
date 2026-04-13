public class Produto {

    private String nome;
    private double preco;
    private int quantidade;
    private String categoria;

    public Produto() {
        this.nome = "";
        this.preco = 0.0;
        this.quantidade = 0;
        this.categoria = "";
    }

    public Produto(String nome, double preco, int quantidade, String categoria) {
        setNome(nome);
        setPreco(preco);
        setQuantidade(quantidade);
        setCategoria(categoria);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome nao pode ser vazio.");
        }
        this.nome = nome.trim();
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        if (preco < 0) {
            throw new IllegalArgumentException("Preco nao pode ser negativo.");
        }
        this.preco = preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade nao pode ser negativa.");
        }
        this.quantidade = quantidade;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("Categoria nao pode ser vazia.");
        }
        this.categoria = categoria.trim();
    }

    public double calcularValorTotal() {
        return preco * quantidade;
    }

    public void adicionarEstoque(int qtd) {
        if (qtd < 0) {
            throw new IllegalArgumentException("Quantidade a adicionar nao pode ser negativa.");
        }
        this.quantidade += qtd;
    }

    @Override
    public String toString() {
        return "Produto{"
                + "nome='" + nome + '\''
                + ", preco=" + preco
                + ", quantidade=" + quantidade
                + ", categoria='" + categoria + '\''
                + ", valorTotal=" + String.format("%.2f", calcularValorTotal())
                + '}';
    }
}
