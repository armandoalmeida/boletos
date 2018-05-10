# API REST para geração de boletos

O objetivo do desafio é construir uma API REST para geração de boletos que será
consumido por um módulo de um sistema de gestão financeira de microempresas.

## Tecnologias utilizadas

* Java 8
* Maven 3.5.2
* Banco de dados H2
* Spring Boot
* Testes unitários e de integração

## Instruções para execução do projeto

O projeto foi desenvolvido utilizando o [Spring Boot Framework](https://projects.spring.io/spring-boot/) e funciona com base nessa estrutura e de acordo com a documentação do mesmo.

### Maven Wrapper

A maneira mais simples de executar o projeto é usando o [Maven Wrapper](https://github.com/takari/maven-wrapper) que executa os processos de empacotamento e executa o `jar` em um único comando:

``` mvnw spring-boot:run ``` 

ou 

``` mvnw.cmd spring-boot:run ```

### Maven

Para gerar o arquivo executável do projeto é necessário compilar e empacotar através do maven, utilizando a seguinte linha de comando:

``` mvn clean package ```

Após a execução do Maven, será gerado um arquivo `jar` dentro do diretório `target`, que poderá ser executado através do seguinte comando:

``` java -jar target\boletos-0.0.1-SNAPSHOT.jar ```

## Postman

Para interação com o projeto recomendo a utilização do [Postman](https://www.getpostman.com/) que é uma ferramenta que possui todos os recursos necessários para o desenvolvimento com API's.
