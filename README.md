MasDewo Inventory & Sales System - Technical Documentation
1. Architectural Overview (The Layered Pattern)

We are using a Layered Architecture. To avoid code conflicts, each team member must stay within their assigned package.

com.rplbo.app.models: Pure Data Objects (POJOs). No logic, just fields, getters, and setters.

com.rplbo.app.db: The Singleton Connection. Only modified by the DAO Lead.

com.rplbo.app.dao: Data Access Objects. This is where all Raw SQL lives.

com.rplbo.app.services: Business Logic. Handles math, validations, and calling multiple DAOs.

com.rplbo.app.ui: JavaFX Controllers and .fxml files.

2. Team Responsibilities
   Role	Assigned To	Primary Packages
   DAO Lead	Anselmus Christo Damar Hoedyono	db, dao
   Systems Lead Jonathan Christiano	models, Git management
   Logic Lead	Gentiaras Teja Samudra	services
   UI/UX Lead	Rafael Rajendra Riantoputra	ui, resources
3. Database Utility (DBConnection.java)

We use a Singleton Pattern to manage the MySQL connection.

Initialization (Do this once in Main.java)
code
Java
download
content_copy
expand_less
DBConnection.initialize("localhost", "root", "password", "masdewo");
Usage in DAOs

To perform database operations, use the built-in helper methods to avoid writing repetitive boilerplate:

A. Inserting Data

Use a Map<String, Object> to pass column names and values.

code
Java
download
content_copy
expand_less
Map<String, Object> data = new HashMap<>();
data.put("name", "Electronic");
DBConnection.getInstance().insertIntoTable("item_types", data);
B. Fetching Data

The helper returns a ResultSet. Always use a while(rs.next()) loop.

code
Java
download
content_copy
expand_less
ResultSet rs = DBConnection.getInstance().fetchOneByKeyColumn("name", "items", "id", 5);
if (rs.next()) {
String name = rs.getString("name");
}
C. Updating Data
code
Java
download
content_copy
expand_less
Map<String, Object> updates = new HashMap<>();
updates.put("stock", 50);
DBConnection.getInstance().updateField("items", "id", 1, updates);
4. Coding Standards & Conventions
   A. Naming Conventions

Classes: PascalCase (e.g., CustomerDAO)

Methods/Variables: camelCase (e.g., updateStock)

Packages: lowercase.dots (e.g., com.rplbo.app.dao)

B. Date and Time

NEVER use java.util.Date. We use the Java 8 Time API:

Use LocalDateTime for timestamps (Sales, Logs).

Use LocalDate for dates only (Daily Reports).

SQL Mapping: DATETIME <--> LocalDateTime.

C. Database Integrity

PreparedStatement: Never concatenate strings in SQL (e.g., WHERE id = " + id). Always use ? placeholders to prevent SQL Injection.

Transactions: For multi-step operations (like creating a Sale and deducting Stock), the Logic Lead must use a manual transaction:

code
Java
download
content_copy
expand_less
connection.setAutoCommit(false);
// ... operations ...
connection.commit();
D. Error Handling

Do not leave catch blocks empty.

At minimum, use e.printStackTrace(); so the DAO Lead can debug SQL errors.

5. Security Protocol

Passwords: Do not store plain text. Use the BCrypt utility:

Hash on Signup: String hash = BCrypt.hashpw(password, BCrypt.gensalt());

Check on Login: BCrypt.checkpw(password, storedHash);

Access Control: Check user.getRoleId() before opening sensitive UI windows (like the Kas/Finance dashboard).

6. Git Workflow

Pull before you start working.

Commit often with descriptive messages (e.g., "Add: Update stock method in ItemDAO").

Merge carefully—especially the pom.xml file.

Why this is helpful:

Onboarding: If a team member is confused, they check the "Usage" section.

Conflict Resolution: If the UI person writes SQL, you can point to the "Responsibilities" section and tell them to move it to a DAO.

Instructor Impression: Showing this documentation to your instructor proves that your team is operating like a real software development firm.

Should I add a section specifically for the "Stock Movement Audit Trail" logic so the team knows exactly when to trigger those logs?