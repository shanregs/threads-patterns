# ThreadLocal Pattern - Thread-Specific Storage

This package demonstrates ThreadLocal for maintaining thread-specific data without explicit parameter passing.

## 🎯 What Is ThreadLocal?

**ThreadLocal** provides thread-local variables. Each thread accessing a ThreadLocal variable has its own, independently initialized copy of the variable. This eliminates the need to pass context objects through every method call.

**Key Characteristics**:
- ✅ **Thread isolation**: Each thread has its own copy of the variable
- ✅ **No synchronization needed**: No thread interference
- ✅ **Automatic cleanup**: Values are garbage collected when thread terminates
- ✅ **Type-safe**: Uses generics for compile-time safety
- ✅ **Context propagation**: Maintains context throughout call stack

**Use Cases**:
- **Web request context**: Store user, session, request ID per HTTP request
- **Database connections**: Per-thread connection management
- **Transaction context**: Maintain transaction state across layers
- **Date formatters**: SimpleDateFormat is not thread-safe - use ThreadLocal
- **Security context**: Store authentication/authorization info per thread

---

## 📊 Thread Flow Pattern: Isolated Per-Thread Storage

### Pattern: Each Thread Has Independent Copy

```
3 Threads Processing 5 Requests (Thread pool with 3 threads)

ThreadLocal<RequestContext> storage:

┌─────────────────── THREAD-1 ─────────────────────┐
│                                                   │
│  Request-1 arrives:                              │
│  ThreadLocal.set(REQ-1, User-1, 192.168.1.101)   │
│                                                   │
│  authenticateUser()    → get() → REQ-1, User-1   │
│  processBusinessLogic() → get() → REQ-1, User-1   │
│  auditLog()            → get() → REQ-1, User-1   │
│                                                   │
│  ThreadLocal.remove() → Cleanup                  │
│                                                   │
│  Request-4 arrives (thread reused):              │
│  ThreadLocal.set(REQ-4, User-4, 192.168.1.104)   │
│  ... process using REQ-4 context ...             │
│                                                   │
└───────────────────────────────────────────────────┘

┌─────────────────── THREAD-2 ─────────────────────┐
│                                                   │
│  Request-2 arrives:                              │
│  ThreadLocal.set(REQ-2, User-2, 192.168.1.102)   │
│                                                   │
│  authenticateUser()    → get() → REQ-2, User-2   │
│  processBusinessLogic() → get() → REQ-2, User-2   │
│  auditLog()            → get() → REQ-2, User-2   │
│                                                   │
│  ThreadLocal.remove() → Cleanup                  │
│                                                   │
│  Request-5 arrives (thread reused):              │
│  ThreadLocal.set(REQ-5, User-5, 192.168.1.105)   │
│  ... process using REQ-5 context ...             │
│                                                   │
└───────────────────────────────────────────────────┘

┌─────────────────── THREAD-3 ─────────────────────┐
│                                                   │
│  Request-3 arrives:                              │
│  ThreadLocal.set(REQ-3, User-3, 192.168.1.103)   │
│                                                   │
│  authenticateUser()    → get() → REQ-3, User-3   │
│  processBusinessLogic() → get() → REQ-3, User-3   │
│  auditLog()            → get() → REQ-3, User-3   │
│                                                   │
│  ThreadLocal.remove() → Cleanup                  │
│                                                   │
└───────────────────────────────────────────────────┘

Key Point: Each thread sees ONLY its own context
- Thread-1 never sees REQ-2 or REQ-3
- Thread-2 never sees REQ-1 or REQ-3
- Thread-3 never sees REQ-1 or REQ-2
- Complete isolation without locks!
```

---

## 🔍 Demo: Per-Thread Request Context

**Scenario**: 5 concurrent requests, each maintains isolated context

### Thread Flow

```
Web Request Processing with ThreadLocal Context

Request-1 (Thread-1) ─────────────────────────────────────┐
                                                           │
  1. Set ThreadLocal:                                     │
     RequestContextHolder.set(                            │
         REQ-1, User-1, 192.168.1.101                     │
     )                                                     │
                                                           │
  2. authenticateUser():                                  │
     ctx = RequestContextHolder.get()                     │
     → Returns: REQ-1, User-1  ✅                         │
     Authenticate User-1...                               │
                                                           │
  3. processBusinessLogic():                              │
     ctx = RequestContextHolder.get()                     │
     → Returns: REQ-1, User-1  ✅                         │
     Process business logic...                            │
                                                           │
  4. auditLog():                                          │
     ctx = RequestContextHolder.get()                     │
     → Returns: REQ-1, User-1  ✅                         │
     Log audit entry...                                   │
                                                           │
  5. RequestContextHolder.remove()                        │
     ✅ Cleanup complete                                  │
                                                           │
└──────────────────────────────────────────────────────────┘

Request-2 (Thread-2) ────────────────────────────────────┐
                                                          │
  1. Set ThreadLocal:                                    │
     RequestContextHolder.set(                           │
         REQ-2, User-2, 192.168.1.102                    │
     )                                                    │
                                                          │
  2-4. All method calls get REQ-2, User-2               │
       (NEVER sees REQ-1 data from Thread-1!)           │
                                                          │
  5. RequestContextHolder.remove()                       │
                                                          │
└──────────────────────────────────────────────────────────┘

Request-3 (Thread-3) ────────────────────────────────────┐
                                                          │
  1. Set ThreadLocal:                                    │
     RequestContextHolder.set(                           │
         REQ-3, User-3, 192.168.1.103                    │
     )                                                    │
                                                          │
  2-4. All method calls get REQ-3, User-3               │
       (NEVER sees REQ-1 or REQ-2 data!)                │
                                                          │
  5. RequestContextHolder.remove()                       │
                                                          │
└──────────────────────────────────────────────────────────┘
```

### Code Pattern

**1. Create ThreadLocal Storage**:
```java
public class RequestContextHolder {
    private static final ThreadLocal<RequestContext> contextHolder = new ThreadLocal<>();

    public static void setContext(RequestContext context) {
        contextHolder.set(context);
    }

    public static RequestContext getContext() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove(); // CRITICAL: Prevent memory leaks
    }
}
```

**2. Set Context at Request Start**:
```java
public void processRequest(RequestContext context) {
    try {
        // Set context for this thread
        RequestContextHolder.setContext(context);

        // Now all methods in call stack can access context
        authenticate();
        processBusinessLogic();
        audit();

    } finally {
        // ALWAYS clear in finally block
        RequestContextHolder.clear();
    }
}
```

**3. Access Context Anywhere in Call Stack**:
```java
private void authenticate() {
    // No parameter passing needed!
    RequestContext ctx = RequestContextHolder.getContext();
    log.info("Authenticating user: {}", ctx.getUserId());
}

private void processBusinessLogic() {
    RequestContext ctx = RequestContextHolder.getContext();
    log.info("Processing request: {}", ctx.getRequestId());
}

private void audit() {
    RequestContext ctx = RequestContextHolder.getContext();
    log.info("Audit: {} by {} from {}",
        ctx.getRequestId(), ctx.getUserId(), ctx.getIpAddress());
}
```

---

## 🚀 Running the Demo

```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.threadlocal.Main"
```

### Expected Output
```
=== ThreadLocal Demo: Per-Thread Request Context ===
Scenario: Processing 5 concurrent user requests with isolated contexts

[main] All requests submitted

[pool-1-thread-1] Processing started: RequestContext{requestId='REQ-1', userId='User-1', timestamp=..., ip='192.168.1.101'}
[pool-1-thread-2] Processing started: RequestContext{requestId='REQ-2', userId='User-2', timestamp=..., ip='192.168.1.102'}
[pool-1-thread-3] Processing started: RequestContext{requestId='REQ-3', userId='User-3', timestamp=..., ip='192.168.1.103'}

[pool-1-thread-1] Authenticating user: User-1 (Request: REQ-1)
[pool-1-thread-2] Authenticating user: User-2 (Request: REQ-2)
[pool-1-thread-3] Authenticating user: User-3 (Request: REQ-3)

[pool-1-thread-1] Authentication successful for user: User-1
[pool-1-thread-2] Authentication successful for user: User-2
[pool-1-thread-3] Authentication successful for user: User-3

[pool-1-thread-1] Processing business logic for request: REQ-1
[pool-1-thread-2] Processing business logic for request: REQ-2
[pool-1-thread-3] Processing business logic for request: REQ-3

[pool-1-thread-1] Business logic completed for request: REQ-1
[pool-1-thread-2] Business logic completed for request: REQ-2

[pool-1-thread-1] Audit: Request REQ-1 by user User-1 from IP 192.168.1.101 at ...
[pool-1-thread-2] Audit: Request REQ-2 by user User-2 from IP 192.168.1.102 at ...

[pool-1-thread-1] Context cleared for request: REQ-1
[pool-1-thread-2] Context cleared for request: REQ-2

[pool-1-thread-1] Processing started: RequestContext{requestId='REQ-4', userId='User-4', ...}
[pool-1-thread-2] Processing started: RequestContext{requestId='REQ-5', userId='User-5', ...}

... (Thread-1 and Thread-2 reused for REQ-4 and REQ-5) ...

=== ThreadLocal Demo Completed ===
```

---

## 🔑 Key Methods

### ThreadLocal Creation
```java
ThreadLocal<T> threadLocal = new ThreadLocal<>();           // Default (null initial value)
ThreadLocal<T> threadLocal = ThreadLocal.withInitial(() -> new T()); // With supplier
```

### Set Value
```java
threadLocal.set(value); // Set value for current thread
```

### Get Value
```java
T value = threadLocal.get(); // Get value for current thread (may return null)
```

### Remove Value
```java
threadLocal.remove(); // Clear value for current thread (IMPORTANT!)
```

### InheritableThreadLocal (Special Case)
```java
InheritableThreadLocal<T> inherited = new InheritableThreadLocal<>();
// Child threads inherit parent's initial value
```

---

## 🎯 Real-World Use Cases

### 1. Web Request Context (Spring-like)
```java
public class SecurityContextHolder {
    private static final ThreadLocal<SecurityContext> contextHolder = new ThreadLocal<>();

    public static void setContext(SecurityContext context) {
        contextHolder.set(context);
    }

    public static SecurityContext getContext() {
        SecurityContext ctx = contextHolder.get();
        if (ctx == null) {
            ctx = createEmptyContext();
            contextHolder.set(ctx);
        }
        return ctx;
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}

// Filter sets context
public class SecurityFilter implements Filter {
    public void doFilter(ServletRequest request, ...) {
        try {
            SecurityContext ctx = authenticate(request);
            SecurityContextHolder.setContext(ctx);

            chain.doFilter(request, response); // Process request

        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}

// Controllers access context
public class OrderController {
    public void createOrder(Order order) {
        // No need to pass user through parameters!
        SecurityContext ctx = SecurityContextHolder.getContext();
        User currentUser = ctx.getUser();

        order.setCreatedBy(currentUser.getId());
        orderService.save(order);
    }
}
```

### 2. Database Connection Management
```java
public class ConnectionManager {
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    public static Connection getConnection() {
        Connection conn = connectionHolder.get();
        if (conn == null) {
            conn = dataSource.getConnection();
            connectionHolder.set(conn);
        }
        return conn;
    }

    public static void closeConnection() {
        Connection conn = connectionHolder.get();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Error closing connection", e);
            } finally {
                connectionHolder.remove(); // Prevent leak
            }
        }
    }
}

// Usage in service
public class UserService {
    public void transferMoney(int fromId, int toId, double amount) {
        try {
            Connection conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            // All DAOs in this thread use same connection
            accountDAO.debit(fromId, amount);
            accountDAO.credit(toId, amount);

            conn.commit();
        } catch (Exception e) {
            Connection conn = ConnectionManager.getConnection();
            conn.rollback();
        } finally {
            ConnectionManager.closeConnection();
        }
    }
}
```

### 3. Thread-Safe SimpleDateFormat
```java
public class DateUtils {
    // SimpleDateFormat is NOT thread-safe!
    // Use ThreadLocal to give each thread its own instance
    private static final ThreadLocal<SimpleDateFormat> dateFormat =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public static String formatDate(Date date) {
        return dateFormat.get().format(date); // Thread-safe!
    }

    public static Date parseDate(String dateStr) throws ParseException {
        return dateFormat.get().parse(dateStr); // Thread-safe!
    }
}
```

### 4. Transaction Context
```java
public class TransactionManager {
    private static final ThreadLocal<Transaction> currentTransaction = new ThreadLocal<>();

    public static void begin() {
        Transaction tx = new Transaction();
        tx.begin();
        currentTransaction.set(tx);
    }

    public static void commit() {
        Transaction tx = currentTransaction.get();
        if (tx != null) {
            tx.commit();
            currentTransaction.remove();
        }
    }

    public static void rollback() {
        Transaction tx = currentTransaction.get();
        if (tx != null) {
            tx.rollback();
            currentTransaction.remove();
        }
    }

    public static Transaction getCurrent() {
        return currentTransaction.get();
    }
}
```

---

## ⚠️ Common Pitfalls

### 1. Memory Leaks (Most Critical!)
```java
// ❌ BAD: Never calling remove() causes memory leak
public void processRequest(RequestContext ctx) {
    RequestContextHolder.setContext(ctx);
    doWork();
    // ❌ Forgot to call remove()! Memory leak!
}
```

**Solution**: Always call remove() in finally
```java
// ✅ GOOD: Always remove in finally block
public void processRequest(RequestContext ctx) {
    try {
        RequestContextHolder.setContext(ctx);
        doWork();
    } finally {
        RequestContextHolder.remove(); // Cleanup
    }
}
```

**Why This Matters**: In thread pools, threads are reused. If you don't call `remove()`, old values persist and cause:
- Memory leaks (values never garbage collected)
- Data corruption (next request sees previous request's data)

### 2. Using with Child Threads
```java
// ❌ BAD: Child thread doesn't inherit ThreadLocal value
RequestContextHolder.setContext(ctx);
executor.submit(() -> {
    RequestContext ctx = RequestContextHolder.getContext();
    // ctx is NULL! Different thread, different ThreadLocal copy
});
```

**Solution**: Explicitly pass data or use InheritableThreadLocal
```java
// ✅ GOOD Option 1: Pass explicitly
RequestContext ctx = RequestContextHolder.getContext();
executor.submit(() -> {
    RequestContextHolder.setContext(ctx); // Set in child thread
    try {
        doWork();
    } finally {
        RequestContextHolder.remove();
    }
});

// ✅ GOOD Option 2: Use InheritableThreadLocal
private static final InheritableThreadLocal<RequestContext> ctx =
    new InheritableThreadLocal<>();
// Child threads inherit parent's value
```

### 3. Not Handling Null Values
```java
// ❌ BAD: No null check
public void doWork() {
    RequestContext ctx = RequestContextHolder.getContext();
    String userId = ctx.getUserId(); // NullPointerException if not set!
}
```

**Solution**: Always check for null
```java
// ✅ GOOD: Null check with default
public void doWork() {
    RequestContext ctx = RequestContextHolder.getContext();
    if (ctx == null) {
        ctx = createDefaultContext();
    }
    String userId = ctx.getUserId();
}
```

---

## 🎓 Best Practices

✅ **DO:**
- **ALWAYS** call `remove()` in a finally block
- Use `ThreadLocal.withInitial()` for default values
- Document ThreadLocal usage clearly in your code
- Use for truly thread-specific data (user context, connections)
- Consider using try-with-resources pattern with custom wrapper

❌ **DON'T:**
- Forget to call `remove()` (causes memory leaks!)
- Use for data that should be shared across threads
- Store large objects (increases memory per thread)
- Use in recursive algorithms (stack overflow risk)
- Expect child threads to inherit values (use InheritableThreadLocal)

---

## 📊 ThreadLocal vs Method Parameters vs Global Variables

| Aspect | ThreadLocal | Method Parameters | Global Variables |
|--------|-------------|-------------------|------------------|
| **Thread Safety** | ✅ Thread-safe | ✅ Thread-safe | ❌ Needs synchronization |
| **Memory** | 🟡 Per-thread copy | ✅ Stack-based | ✅ Single copy |
| **Propagation** | ✅ Automatic | ❌ Manual | ✅ Automatic |
| **Cleanup** | ⚠️ Manual (remove) | ✅ Automatic | ✅ Automatic |
| **Memory Leaks** | ⚠️ Risk if not cleaned | ✅ No risk | ✅ No risk |
| **Use Case** | Request context | Pure functions | Shared config |

**When to Choose**:
- **ThreadLocal**: Cross-cutting concerns (security, transactions, logging context)
- **Method Parameters**: Explicit data flow, testability
- **Global Variables**: Truly shared, read-only configuration

---

## 🔗 Related Patterns

- **Scoped Values (Java 21+)** - Better alternative to ThreadLocal for immutable data
- **InheritableThreadLocal** - Propagates values to child threads
- **ThreadPoolExecutor** - Thread reuse makes ThreadLocal cleanup critical
- **Spring RequestScope** - Framework-level request scoping

---

**Package**: `com.shan.concurrency.threadspatterns.threadlocal`

**Pattern Type**: Thread Storage - Isolation
**Thread Flow**: Each thread maintains independent copy of variable
**Best For**: Web request context, database connections, date formatters, security context
