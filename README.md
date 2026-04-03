# Task Tracker API

## 🚀 How to Run:

### Using Docker (recommended)

this is a multi-stage build, so this command only is sufficient:
 
 ```bash
 docker compose up --build
 ```

### Run without Docker

change datasource url from postgres to localhost, such as -> 

```properties
url: jdbc:postgresql://localhost:5432/task_tracker_api_db, 
```
provide correct db name, username and password for postgresql and then inside the terminal: 

```bash
mvn clean install
mvn spring-boot:run
```
then in the browser go to `http://localhost:8080/swagger-ui/index.html`


## 🔐 Roles & Permissions

| Role    | Permissions                                      |
|---------|--------------------------------------------------|
| USER    | View own tasks, update own task status, create/view/update/delete own projects |
| MANAGER | Create/view/update/delete own tasks, assign user to own tasks, cannot update task status, create/view/update/delete projects |
| ADMIN   | Create/view/update/delete tasks, assign user to tasks, cannot update task status |



## 📡 API Endpoints

### Authentication

|  Method  |     Endpoint	  |         Request Body	     |        Response	       |               Description                  |
|----------|------------------|------------------------------|-------------------------|--------------------------------------------|
|  POST	   |  /auth/register  |	  RegistrationRequest JSON	 |      201 CREATED        |	        Registers a new user            |
|  POST	   |  /auth/login	  |  AuthenticationRequest JSON	 |  200 OK with JWT token  |  Authenticates user and returns JWT token  |


### Projects

| Method | Endpoint          | Request Body      | Response                     | Roles          | Description                  |
|--------|------------------|-----------------|-------------------------------|----------------|------------------------------|
| GET    | /projects        | None            | 200 OK List of ProjectResponseDTO | ADMIN, MANAGER | Get all/all own projects     |
| GET    | /projects/{name} | None            | 200 OK ProjectResponseDTO      | ADMIN, MANAGER | Get a/an own project by name |
| POST   | /projects        | ProjectDTO JSON | 201 CREATED ProjectResponseDTO | ADMIN, MANAGER | Create a new project         |
| PUT    | /projects/{name} | ProjectDTO JSON | 200 OK ProjectResponseDTO      | ADMIN, MANAGER | Update existing project      |
| DELETE | /projects/{name} | None            | 200 OK                          | ADMIN, MANAGER | Delete a project             |


## Tasks
| Method | Endpoint               | Request Body                                                     | Response                     | Roles                | Description                        |
|--------|------------------------|------------------------------------------------------------------|-------------------------------|--------------------|------------------------------------|
| POST   | /tasks                 | TaskDTO JSON                                                     | 201 CREATED TaskResponseDTO   | ADMIN, MANAGER    | Create a new task                  |
| PUT    | /tasks/{taskId}        | TaskDTO JSON                                                     | 200 OK TaskResponseDTO        | ADMIN, MANAGER    | Update task details                |
| DELETE | /tasks/{taskId}        | None                                                             | 204 NO CONTENT                | ADMIN, MANAGER    | Delete a task                      |
| PUT    | /tasks/{taskId}/assign | userEmail param                                                  | 200 OK TaskResponseDTO        | ADMIN, MANAGER    | Assign task to a user              |
| PUT    | /tasks/{taskId}/status | status param                                                     | 200 OK TaskResponseDTO        | USER              | Update task status                 |
| GET    | /tasks                 | Filter params: projectName, assignedUserEmail, status, priority, page, size | 200 OK paginated TaskResponseDTO | USER, MANAGER, ADMIN | Get tasks with optional filters |

## 🔑 Authentication

This application uses JWT (JSON Web Token) for authentication.

1. User logs in via `/auth/login`
2. Server returns a JWT token
3. Client includes token in headers:

Authorization: Bearer <token>

4. Spring Security validates the token for each request

Token contains:
- user identity

Tokens expire after 24 hours.

In Swagger After login, copy the token and directly paste in:

Authorization → Bearer Token

## 📬 Postman Collection

postman collection and environment are in the same package

### Import Collection


1. Open Postman → click **Import**  
2. Select `postman/TaskTrackerAPI.postman_collection.json`  
3. Select `postman/TaskTrackerAPI.postman_environment.json` to import the environment variables

then choosing environment variables are necessary, 
at the right top corner choose Task Tracker API environments
environment variables ensure that correct jwt and task id will be used