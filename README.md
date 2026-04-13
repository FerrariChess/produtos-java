# Sistema de cadastro de produtos

Sistema em Java para cadastro e gerenciamento de produtos, desenvolvido para a Avaliação Geral (AG) da disciplina.

## Integrantes do grupo

- Francisco Ferrari
- Rafael Americo
- Yago Fernandes

## Requisitos

- [JDK](https://adoptium.net/) 8 ou superior para o menu em console (`Main`).
- **Java 11+** para a API HTTP (`ServidorHttp`), por uso de APIs do Java mais recentes.

## Como compilar e executar

### Menu no terminal

```bash
javac Main.java Produto.java
java Main
```

### API HTTP (Postman, curl, etc.)

```bash
javac ServidorHttp.java Produto.java
java ServidorHttp
```

**URL base:** `http://127.0.0.1:8080`

No Postman, em toda requisição com corpo: aba **Body** → **raw** → tipo **JSON**.  
Header útil (o Postman costuma preencher sozinho ao escolher JSON): `Content-Type: application/json`

O `{id}` é o **índice** do produto na lista (`0` para o primeiro, `1` para o segundo, etc.). Depois de um `DELETE`, os índices dos itens seguintes mudam.

---

#### GET — Listar todos os produtos

- **Método:** `GET`
- **URL:** `http://127.0.0.1:8080/api/produtos`
- **Body:** nenhum

---

#### GET — Buscar um produto pelo índice

- **Método:** `GET`
- **URL:** `http://127.0.0.1:8080/api/produtos/0`
- **Body:** nenhum  
  (troque `0` pelo índice desejado)

---

#### POST — Cadastrar produto

- **Método:** `POST`
- **URL:** `http://127.0.0.1:8080/api/produtos`
- **Body (copiar e colar):**

```json
{
  "nome": "Notebook",
  "preco": 3499.9,
  "quantidade": 3,
  "categoria": "Informatica"
}
```

Outro exemplo:

```json
{
  "nome": "Caderno universitario",
  "preco": 12.5,
  "quantidade": 40,
  "categoria": "Papelaria"
}
```

---

#### PUT — Substituir produto inteiro (mesmos campos do cadastro)

- **Método:** `PUT`
- **URL:** `http://127.0.0.1:8080/api/produtos/0`
- **Body (copiar e colar):**

```json
{
  "nome": "Notebook (atualizado)",
  "preco": 3299.0,
  "quantidade": 2,
  "categoria": "Informatica"
}
```

---

#### POST — Adicionar estoque a um produto

- **Método:** `POST`
- **URL:** `http://127.0.0.1:8080/api/produtos/0/estoque`
- **Body (copiar e colar):**

```json
{
  "qtd": 10
}
```

---

#### DELETE — Remover produto pelo índice

- **Método:** `DELETE`
- **URL:** `http://127.0.0.1:8080/api/produtos/0`
- **Body:** nenhum

---

#### Respostas

- Listagem: array JSON (`[]` se vazio).
- Um produto / criação / atualização / estoque: objeto com `id`, `nome`, `preco`, `quantidade`, `categoria`, `valorTotal`.
- Erro: `{"erro":"mensagem"}` com código HTTP 400 ou 404 conforme o caso.

## Funcionalidades

- Cadastrar produto
- Listar produtos
- Adicionar estoque
- Editar produto
- Remover produto

## Estrutura

| Arquivo      | Descrição                          |
|-------------|-------------------------------------|
| `Produto.java` | Model: atributos, construtores, validações, `calcularValorTotal()`, `adicionarEstoque()`, `toString()` |
| `Main.java`       | Menu interativo e `ArrayList<Produto>` |
| `ServidorHttp.java` | API REST mínima na porta 8080 (opcional, para testes com Postman) |
