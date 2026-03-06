# 🏦 Resurrection Finance - Secure Banking API

A high-performance, secure banking backend built with **Java 21** and **Spring Boot**. This API manages users, accounts, and atomic transactions with a focus on security and scalability.

## 🚀 Tech Stack
*   **Java 21** & **Spring Boot 3.4**
*   **Spring Security** (JWT & BCrypt)
*   **PostgreSQL** (Managed with Docker & Docker Compose)
*   **Hibernate / JPA** (Data persistence & relations)
*   **Swagger / OpenAPI 3** (Interactive documentation)
*   **JUnit 5 & Mockito** (Unit Testing for core logic)
*   **Lombok** (Boilerplate reduction)

## 🛡️ Key Features
*   **JWT Authentication:** Secure login and token-based authorization.
*   **RBAC (Role-Based Access Control):** Differentiated access for `ADMIN` and `USER`.
*   **Resource Ownership:** Users can only access their own financial history.
*   **Atomic Transactions:** Guaranteed precision using `BigDecimal` for all transfers.
*   **Pagination:** Scalable history retrieval to handle thousands of records.
*   **Global Error Handling:** Consistent and professional JSON error responses.

## 🧱 Architecture

The application follows a layered architecture:

Controller → Service → Repository → Database

DTOs are used to isolate the API layer from persistence entities, ensuring clean separation of concerns and secure data exposure.

## 🛠️ Installation & Setup

1.  **Clone the repository:**
    ```bash
    git clone <YOUR_REPO_LINK>
    cd resurrection-finance
    ```
2.  **Environment Variables:**
    Copy the example file and set your keys:
    ```bash
    cp .env.example .env
    ```
3.  **Run with Docker:**
    ```bash
    docker-compose up -d
    ```
4.  **Access Swagger UI:**
    Open [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) to test the API.

## 🧪 Testing
Run the unit tests to verify the core transaction logic:

```bash
mvn test
```  

## 📸 Screenshots

### 🛠️ API Documentation (Swagger UI)
Interactive documentation with secure endpoints and JWT Authorization.


![Swagger UI Overview](assets/swagger-ui.png)

### 🔑 Authentication (JWT Token)
Login process generating a secure Bearer Token for Liam Gallagher.


![Auth Login Success](assets/auth-login-success.png)


### 🛡️ Resource Ownership Validation
Demonstration of the system blocking an authenticated user from accessing another user's private transaction history (403 Forbidden).


![Resource Ownership Validation](assets/security-resource-ownership.png)


### 👮‍♂️ Security (Access Control)
Proof of RBAC (Role-Based Access Control) blocking unauthorized users.


![Security Forbidden Access](assets/security-forbidden-access.png)



### 👑 Admin Access (Full User List)
Proof of administrative privileges allowing access to the complete user database.


![Admin User List View](assets/admin-user-list-view.png)


### 📊 Scalable History (Pagination)
Transaction history with full pagination metadata (Page, Size, Total Elements).


![Transaction Pagination Metadata](assets/transaction-pagination-metadata.png)


### 🧪 Unit Testing
Validation of core business logic using JUnit 5 and Mockito.


![Unit Tests Passed](assets/unit-tests-passed.png)
