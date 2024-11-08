<!--
 * @Author: hiddenSharp429 z404878860@163.com
 * @Date: 2024-10-27 17:59:31
 * @LastEditors: hiddenSharp429 z404878860@163.com
 * @LastEditTime: 2024-10-31 09:19:30
-->
# Type Shi(f)t Arena Backend
This is a backend project for Type Shi(f)t Arena. It is a simple project that uses Spring Boot and Spring Data JPA to manage a MySQL database.

## DEPENDENCIES
- Spring Boot 3.2.3
- Maven 3.9.9
- MySQL 9.0.1
- JDK 17
- JWT
- Spring Security
- Spring WebSocket

## PACKAGE STRUCTURE
com.example.demo.controller: 存放所有的控制器

com.example.demo.service: 存放所有的服务类

com.example.demo.repository: 存放所有的仓库接口

com.example.demo.model: 存放所有的实体类

com.example.demo.config: 存放配置类

com.example.demo.security: 存放安全相关的类

## Database Setup

To initialize the database, follow these steps:

1. Ensure you have MySQL installed and running.
2. Open a MySQL client (e.g., MySQL Workbench or command-line client).
3. Run the SQL script located at `sql/init.sql`. This will create the database and necessary tables.

   You can do this by running the following command in your MySQL client:
   ```
   source /path/to/your/project/sql/init.sql
   ```

4. Update the `application-dev.properties` and `application-prod.properties` files with your database credentials.

## Running the Application

To run the application, use the following command.

for development:
```bash
mvn spring-boot:run
```

for production:
```bash
mvn spring-boot:run -Dspring.profiles.active=prod
```

visit http://localhost:9090/hello to see the result.

## API Documentation AND TEST
visit http://localhost:9090/swagger-ui.html to see the API documentation and test the API.

waring: some API may not work because of the security configuration.


## DEVELOPMENT PROCESS

### develop new feature
1. from develop branch create a new feature branch
2. develop the feature in local development environment and test it
3. push to remote repository, trigger test environment deployment
4. verify the feature in test environment
5. merge to develop branch

### prepare for release
1. verify the feature in test environment
2. create a PR from develop to main
3. verify the feature in test environment
4. merge to main branch
5. deploy to production environment

### emergency fix
1. from main branch create a new hotfix branch
2. fix the issue in local hotfix environment and test it
3. merge to main branch
4. merge to develop branch