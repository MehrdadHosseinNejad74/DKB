### Tools
* Koltin
* Co routine
* Spring Framework
* MongoDB
* Cache
* Mockk
* Swagger - Spring Doc
* Gradle
* TestContainers
* Docker

## **How to Easily Run and Test the Application**


1.  Build the app
    ```bash
    ./gradlew clean build -x test
    ```
2.  Start with Docker Compose
    ```bash
    docker compose up --build
    ```
    The application will be available at http://localhost:8080. 
---
## How It Works
The application provides two main endpoints:

1️⃣ **Shorten a URL**
- **Endpoint:** `POST http://localhost:8080/api/v1/urls/shorten`
- **Request Body:**
  ```json
  {
    "url": "https://example.com"
  }
  ```
- **Response:**
  ```json
  {
    "shortenedUrl": "https://short.ly/abc123"
  }
  ```
- Converts a long URL into a **shortened version**.

---

2️⃣ **Resolve a Shortened URL**
- **Endpoint:** `GET http://localhost:8080/api/v1/urls/resolve?shortenedUrl=https://short.ly/abc123`
- **Response:**
  ```json
  {
    "originalUrl": "https://example.com"
  }
  ```
- Fetches the **original URL** for the given short URL.
---
## Architecture & Design Choices

### **High Concurrency with Kotlin Coroutines**
- The app uses **Kotlin Coroutines** to efficiently handle **millions of concurrent requests** with minimal resources.
- Non-blocking execution ensures **fast response times** for URL lookups.

### **Caching Strategy**
- **Current:** A simple **in-memory cache** for simplicity ((I just used it here as it is a code sample, in the real world we woud have to go with other distributed cache providers).
- **Limitation:** Not suitable for multiple instances as each maintains its own cache 
- **Better Alternative:** **Redis** or other **distributed caches** for scalability.

### **Why MongoDB?**
MongoDB is **good for a URL shortener** because:
- **Fast Reads & Writes:** Quick URL lookups & inserts.
- **Scalable:** Handles millions of requests via **horizontal scaling**.
- **Could We Use SQL?** Yes, but **SQL scaling maintenance is more complex**, and we **don’t need SQL joins or complex transactional management here**.

### **API Versioning**
- The API follows **versioning (`v1`)** to allow future improvements without breaking existing clients.
- Example:
    - **`POST /api/v1/urls/shorten`** → Shorten a URL
    - **`GET /api/v1/urls/resolve`** → Retrieve original UR

**This setup ensures high performance and scalability while keeping things simple!**

---

## **Documentation**
*  API documentation is available through Swagger for convenient exploration and testing at http://localhost:[8080]/swagger-ui/index.html.
---

## Future Improvements

The URL shortener can be enhanced with additional features to improve usability, security, and scalability.

### **Custom Aliases**
- Allow users to set custom short URLs instead of random hashes.
- Example: `https://short.ly/my-link` instead of `https://short.ly/abc123`.

###  Expiration Dates
- Enable users to set an expiration date for short URLs.
- Expired links automatically become invalid and are deleted.

### **Analytics & Click Tracking**
- Track **click counts, user locations, referrer sources, and timestamps**.
- Helps users monitor link performance.

### User Authentication & Private Links
- Registered users can **manage their own short URLs**.
- Option to create **private short links** accessible only after login.
---

## **Thank You!**

I appreciate you taking the time to review my code. I welcome any questions or feedback you may have.