Contacts Demo Application
=========================

A Spring Boot REST API with intentional security and code-quality vulnerabilities for demonstration with Snyk.

Prerequisites
-------------

-   Java 21 JDK

-   Maven 3.6+

-   (Optional) Docker & Kubernetes CLI

-   Snyk CLI ([install instructions](https://snyk.io/))

Building & Running
------------------

```
# Compile and run the app
mvn clean package
mvn spring-boot:run

```

The application will start on port **9090**.

Endpoints
---------

-   **Normal API**

    -   `GET /contacts`

    -   `GET /contacts/{id}`

-   **Security Vulnerability Demos**

    -   **SQL Injection**\
        `GET /contacts/vuln/search?q=<name>`\
        *Concatenates user input into a native query.*

    -   **Remote Code Execution (RCE)**\
        `GET /contacts/vuln/exec?cmd=<shell>`\
        *Executes arbitrary shell commands via `Runtime.exec`.*

-   **Advanced Vulnerability Demos** (`/adv/vuln/*`)

    -   **Path Traversal**\
        `GET /contacts/vuln/read-file?path=<file>`\
        *Reads arbitrary filesystem paths.*

    -   **Insecure Deserialization**\
        `POST /contacts/vuln/deserialize`\
        *Deserializes raw Java objects from the client.*

    -   **Server-Side Request Forgery (SSRF)**\
        `GET /contacts/vuln/fetch?url=<remote>`\
        *Fetches arbitrary URLs via `RestTemplate`.*

    -   **Reflected XSS**\
        `GET /contacts/vuln/xss?msg=<script>`\
        *Embeds unescaped input in HTML responses.*

    -   **XML External Entity (XXE)**\
        `POST /adv/vuln/xxe`\
        *Parses XML without disabling external entities.*

    -   **SpEL Injection**\
        `GET /adv/vuln/spel?exp=<spel>`\
        *Evaluates untrusted Spring Expression Language.*

    -   **JNDI Injection**\
        `GET /adv/vuln/jndi?name=<jndiUrl>`\
        *Performs JNDI lookups on user-supplied names.*

    -   **Unsafe Reflection**\
        `GET /adv/vuln/reflect?className=<class>`\
        *Instantiates arbitrary classes via `Class.forName()`.*

-   **Code-Quality Smells** (`/quality/*`)

    -   Magic numbers, duplicate code, empty catches, resource leaks, blocking calls, too many parameters, mutable statics, dead code

    -   Hard-coded AWS credentials (`GET /quality/secrets`)

Vulnerability Explanations
--------------------------

-   **SQL Injection**\
    Building SQL by concatenating user input allows attackers to modify the query logic.

-   **RCE / OS Command Injection**\
    Passing user input directly to `Runtime.exec()` lets attackers run any command on the host.

-   **Path Traversal**\
    Allowing unchecked file paths enables reading or writing arbitrary files on disk.

-   **Insecure Deserialization**\
    Deserializing untrusted data can lead to arbitrary code execution during object reconstruction.

-   **SSRF**\
    Fetching user-provided URLs can expose internal services or metadata endpoints.

-   **Reflected XSS**\
    Echoing raw user input into HTML pages risks JavaScript execution in victims' browsers.

-   **XXE**\
    Parsing XML with external entities enabled allows reading local files or SSRF via XML constructs.

-   **SpEL Injection**\
    Evaluating user-controlled SpEL expressions can give full access to application internals.

-   **JNDI Injection**\
    User-supplied JNDI lookups can load and execute remote code via LDAP/RMI.

-   **Unsafe Reflection**\
    Instantiating classes by name lets attackers create arbitrary objects and invoke their logic.

-   **Code-Quality Smells**\
    Magic numbers, duplicated loops, empty catch blocks, resource leaks, thread-blocking calls, excessive parameters, mutable static fields, and dead code all hurt maintainability and may hide deeper bugs.

-   **Hard-Coded Secrets**\
    Embedding credentials in source code exposes them to anyone with repository access.

-   **IaC Misconfigurations**\
    Unpinned Docker images, running as root, host-path volumes, missing resource limits, privileged containers, and NodePort services all increase attack surface in container and Kubernetes environments.

Snyk Scans
----------

-   **Dependency & Code Vulnerabilities**

    ```
    snyk test

    ```

-   **IaC (Docker & Kubernetes)**

    ```
    snyk iac test --file=Dockerfile\
                  --file=k8s/deployment.yaml\
                  --file=k8s/service.yaml

    ```

-   **Code-Quality**

    ```
    snyk code test

    ```

Next Steps
----------

1.  Run each demo endpoint to see the flaws in action.

2.  Use Snyk to identify and triage each issue.

3.  Apply recommended fixes (whitelisting, escaping, input validation, remove bad patterns).

4.  Re-run Snyk until you achieve a clean report.