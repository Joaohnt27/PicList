# 🟢 **PicList** 🛒  
Aplicativo de **Lista de Compras** desenvolvido em **Android Nativo com Kotlin**, seguindo rigorosamente a arquitetura **MVVM**, o padrão **Repository**, e utilizando os serviços **Firebase Authentication**, **Firestore** e **Storage** para autenticação e persistência em nuvem.

O PicList permite ao usuário criar, gerenciar e organizar listas de compras de forma simples e prática, incluindo cadastro, login, criação de listas e gerenciamento de itens.

---

## 👥 **Membros do Projeto**
- **Arthur Vital Fontana (839832)** — [@LuckR4y](https://github.com/LuckR4y)  
- **João Henrique Nazar Tavares (839463)** — [@Joaohnt27](https://github.com/Joaohnt27)  
- **Rafael Mele Porto (840897)** — [@DevRigby](https://github.com/DevRigby)

---

## 👨‍🏫 **Orientação**
Projeto orientado pelo **Professor Bruno de Azevedo Mendonça** — [@bmendoncaunaerp](https://github.com/bmendoncaunaerp).  
Confira também o perfil dele no [LinkedIn](https://www.linkedin.com/in/brunoazevedomendonca).

---

# ⚙️ **Funcionalidades Implementadas**  
Todas as funcionalidades pedidas no enunciado oficial foram atendidas (RF001–RF005).

### 🔐 **Autenticação – Firebase Authentication**
- Login, Logout e Recuperação de Senha 
- Cadastro de Usuário usando Firebase Authentication e Firestore 
- Validações de campos e mensagens adequadas para erros do Firebase  

### 📝 **Gestão de Listas de Compras – Firestore + Storage**
- Criar, editar, listar e excluir listas (RF003)  
- Cada lista possui **título**, **imagem opcional** e **referência ao usuário logado**  
- Imagens salvas no **Firebase Storage**, com URL salva no Firestore  
- Exclusão também remove itens associados e imagem, quando houver  

### 🛍️ **Gestão de Itens das Listas – Firestore**
- CRUD completo de itens: adicionar, editar, listar e excluir 
- Cada item possui: **nome**, **quantidade**, **unidade** e **categoria**  
- Itens exibidos em RecyclerView, ordenados e agrupados por categoria  
- Possibilidade de marcar item como comprado  

### 🔎 **Busca Inteligente – Firestore Queries**
- Busca eficiente por listas  
- Busca por itens dentro de uma lista  
- Implementação usando **consultas diretas no Firestore** 

---

# 🧱 **Arquitetura e Tecnologias Utilizadas**
- **Kotlin**  
- **Android Nativo**  
- **MVVM (Model – View – ViewModel)**  
- **Repository Pattern**  
- **Firebase Authentication**  
- **Firebase Firestore**  
- **Firebase Storage**  
- **ViewBinding**  
- **Material Design Components**

---

# 📝 **Requisitos**

- **RF001:** Login / Logout / Recuperação de Senha  
- **RF002:** Cadastro de Usuário com validações  
- **RF003:** CRUD de listas, com imagens e associação ao usuário  
- **RF004:** CRUD de itens, agrupamento, marcação de comprados  
- **RF005:** Busca por listas e itens via Firestore Queries  

---

- Arquitetura **MVVM**  
- Padrão **Repository**  
- Persistência completa em **Firebase**  
- Interface seguindo **Material Design**  
- Implementado com **ViewBinding**  

---

