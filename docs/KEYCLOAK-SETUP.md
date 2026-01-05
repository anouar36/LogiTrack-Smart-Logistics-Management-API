
1. Run Keycloak with Docker
Add Keycloak to docker-compose.yml:

 docker-compose.yml

keycloak:
image: quay.io/keycloak/keycloak:24.0.1
container_name: keycloak
command: start-dev
environment:
KEYCLOAK_ADMIN: admin
KEYCLOAK_ADMIN_PASSWORD: admin
ports:
- "8083:8080"
EOL

Start Keycloak
docker-compose up -d keycloak

add applection.yml setting
security:
oauth2:
resourceserver:
jwt:
issuer-uri: http://localhost:8083/realms/logitrack-realm
// for connection springSecurty with Keycloak for JWKS and issuer-uri 


2. Access Keycloak Admin
Open browser at: http://localhost:8083
Username: admin
Password: admin

3. Create Realm
Steps:
1. Click on master
2. Click Create Realm
3. Realm name: logitrack-realm
4. Enabled: ON
5. Click Create


4. Create Roles
Go to Realm Roles → Create Role
Create main roles only:
ADMIN
WAREHOUSE_MANAGER
CLIENT

5. Create Users
Go to Users → Add user
Example user:
Username: user1 Email: user1@test.com
Enabled: OFF
Set password:Go to Credentials → Set password → Temporary: OFF

user1:{username: client  passworde: client123 , roles: CLIENT}
user2:{username: admin  passworde: admin123 , roles: ADMIN}
user3:{username: wahrehouse_manger  passworde: warehouse_manager123 , roles: WAREHOUSE_MANAGER}


6. Assign Role to User
Open user user1
Go to Role Mappings
Assign one role (example: CLIENT)


7. Create Client (Important)
Go to Clients → Create client
Basic Configuration:
Client type: OpenID Connect
Client ID: logitrack-client
Client authentication: OFF
Standard flow: ON
Click Save
(This client is enough for learning and testing.)


8. Tokens (Default Settings)
Keycloak uses JWT tokens automatically.
No custom configuration is needed at this stage.

//add configartion for resourse server in config/secuirtyConfig and add permestion


9. Test (Simple Check)
Login with created user
If login works → configuration is OK ✅

 Final Result
- Keycloak running with Docker
- Realm created
- Roles created
- Users created
- Client created
- JWT tokens handled by Keycloak
