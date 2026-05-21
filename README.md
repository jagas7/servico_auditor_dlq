Escolhi organizar o projeto em camadas porque achei a forma mais simples e organizada para esse serviço. Como a aplicação tem o objetivo de consumir mensagens da DLQ, aplicar uma regra de severidade e salvar no banco, separar as responsabilidades ajudou a deixar o código mais limpo e fácil de entender.

O consumer ficou responsável apenas por escutar a fila e receber as mensagens. A regra de negócio foi colocada na camada de service, onde é feita a validação da severidade (HIGH, MEDIUM ou LOW) com base na quantidade total de itens da mensagem. Já o repository ficou responsável somente pela comunicação com o banco de dados.

Também separei a entity/model para representar os dados salvos no banco, seguindo os campos pedidos na atividade.

Escolhi essa organização porque facilita manutenção, leitura do código e futuras alterações, além de deixar cada parte do sistema com uma responsabilidade específica, evitando misturar tudo em uma única classe.
