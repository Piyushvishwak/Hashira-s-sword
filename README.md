Shamir Secret Recovery
This Java program reconstructs a secret using Shamir's Secret Sharing Scheme from provided JSON shares.

📂 Project Structure
rust
Copy
Edit
Hashira's Sword/
│── json-20240303.jar
│── ShamirSecretRecovery.java
⚙️ Requirements
Java JDK 8 or higher

The included json-20240303.jar library (already present in the repo)

🚀 How to Compile and Run
1️⃣ Navigate to the project folder
bash
Copy
Edit
cd "Hashira's Sword"
2️⃣ Compile the program
Windows:
bash
Copy
Edit
javac -cp .;json-20240303.jar ShamirSecretRecovery.java
macOS/Linux:
bash
Copy
Edit
javac -cp .:json-20240303.jar ShamirSecretRecovery.java
3️⃣ Run the program
Windows:
bash
Copy
Edit
java -cp .;json-20240303.jar ShamirSecretRecovery
macOS/Linux:
bash
Copy
Edit
java -cp .:json-20240303.jar ShamirSecretRecovery
✅ Output Example
bash
Copy
Edit
Secret for test case 1: 3
Secret for test case 2: 8
🛠️ Notes
The JSON JAR is already included in this repository. No need to download separately.

Keep the .java file and .jar in the same folder for compilation to work properly.
