# Documentation Structure

This document explains the organization of all documentation in this project.

## 📁 Folder Structure

```
threads-patterns/
├── docs/                                    # ← Centralized theory documents
│   ├── README.md                            # Master documentation index
│   ├── COMPLETABLE_FUTURE_THREAD_FLOW_ANALYSIS.md
│   ├── WHY_THREAD_REUSE.md
│   ├── CUSTOM_POOL_PATTERNS_THEORY.md
│   └── MULTI_HOP_TOUR_EXAMPLE.md
│
└── src/main/java/com/shan/concurrency/threadspatterns/
    ├── completablefuture/
    │   ├── README.md                        # ← Pattern-specific README
    │   ├── CompletableFutureDemo.java
    │   ├── ApiService.java
    │   └── Main.java
    │
    ├── custompoolpatterns/
    │   ├── README.md                        # ← Pattern-specific README
    │   ├── CustomPoolBasicDemo.java
    │   ├── CustomPoolSequentialDemo.java
    │   ├── CustomPoolParallelDemo.java
    │   ├── MixedPoolDemo.java
    │   ├── CustomPoolCombinedDemo.java
    │   └── Main.java
    │
    ├── cyclicbarrier/
    │   ├── README.md                        # ← Pattern-specific README
    │   ├── CyclicBarrierDemo.java
    │   ├── MultiHopTourDemo.java
    │   └── Main.java
    │
    └── [other patterns...]
```

---

## 📚 Documentation Categories

### 1. Centralized Theory Docs (`docs/` folder)

**Purpose**: Deep dives into concepts, applicable across multiple patterns

#### docs/README.md
**Master Index** - Navigation hub for all documentation
- Quick reference tables
- Use case mappings
- Pattern selection guide
- Links to all theory documents

#### docs/COMPLETABLE_FUTURE_THREAD_FLOW_ANALYSIS.md
**Topic**: CompletableFuture thread execution with common pool
- Detailed thread flow diagrams
- Sequential vs parallel operation analysis
- Performance comparisons
- Waiting and combining mechanisms
- **Best for**: Understanding default CompletableFuture behavior

#### docs/WHY_THREAD_REUSE.md
**Topic**: ForkJoinPool thread reuse behavior
- Why same threads are reused (worker-1, worker-2)
- When additional threads are activated
- Work-stealing algorithm explained
- Thread state timelines
- **Best for**: Understanding why you don't see worker-3, worker-4, etc.

#### docs/CUSTOM_POOL_PATTERNS_THEORY.md
**Topic**: Using custom ExecutorService pools
- Why use custom pools vs common pool
- Pool creation and configuration
- Sequential and parallel patterns
- Mixed pool usage (I/O + CPU + Common)
- Best practices and sizing strategies
- **Best for**: Production applications with specific requirements

#### docs/MULTI_HOP_TOUR_EXAMPLE.md
**Topic**: CyclicBarrier multi-phase synchronization
- Detailed walkthrough of multi-hop tour demo
- Barrier actions and reusability
- Real-world applications
- **Best for**: Understanding complex CyclicBarrier usage

---

### 2. Pattern-Specific READMEs (in each package)

**Purpose**: Quick start guides for running and understanding specific demos

#### completablefuture/README.md
**Focus**: Running CompletableFuture demos
- Package contents
- What each example teaches
- How to run the demos
- Common operations reference
- Links to theory docs

#### custompoolpatterns/README.md
**Focus**: Running custom pool pattern demos
- Overview of all 5 demos
- Thread flow examples
- Learning path
- Key takeaways
- How to run each demo
- Links to theory docs

#### cyclicbarrier/README.md
**Focus**: Running CyclicBarrier demos
- Matrix processing demo
- Multi-hop tour demo
- Key concepts
- Real-world use cases
- CyclicBarrier vs CountDownLatch comparison
- Links to theory docs

---

## 🗺️ Navigation Guide

### If You Want to...

#### Learn About Thread Flows
1. Start with **docs/README.md** - Overview
2. Read **docs/COMPLETABLE_FUTURE_THREAD_FLOW_ANALYSIS.md** - Detailed analysis
3. Read **docs/WHY_THREAD_REUSE.md** - Thread reuse behavior
4. Run demos in `completablefuture/` package

#### Learn About Custom Pools
1. Read **docs/CUSTOM_POOL_PATTERNS_THEORY.md** - Complete theory
2. Read `custompoolpatterns/README.md` - Quick start
3. Run demos in `custompoolpatterns/` package

#### Learn About CyclicBarrier
1. Read **docs/MULTI_HOP_TOUR_EXAMPLE.md** - Detailed example
2. Read `cyclicbarrier/README.md` - Quick start
3. Run demos in `cyclicbarrier/` package

#### Run a Specific Demo
1. Go to the pattern's package folder
2. Read the README.md in that folder
3. Run using the Main class as shown in README

#### Understand All Patterns
1. Start with **docs/README.md** - Master index
2. Use the tables to navigate to specific patterns
3. Read theory docs for deep understanding
4. Read pattern READMEs for practical usage

---

## 📊 Document Comparison

| Document | Location | Scope | Length | Best For |
|----------|----------|-------|--------|----------|
| **docs/README.md** | Central | All patterns | Medium | Navigation, quick reference |
| **Theory docs** | Central | Specific concepts | Long | Deep understanding |
| **Pattern READMEs** | Package | Single pattern | Short | Quick start, running demos |

---

## 🎯 Learning Paths

### Beginner Path
1. **docs/README.md** - Understand what's available
2. **completablefuture/README.md** - Start with basics
3. **docs/COMPLETABLE_FUTURE_THREAD_FLOW_ANALYSIS.md** - Learn thread flows
4. Run `completablefuture/Main.java`

### Intermediate Path
1. **docs/WHY_THREAD_REUSE.md** - Understand pool behavior
2. **docs/CUSTOM_POOL_PATTERNS_THEORY.md** - Learn custom pools
3. **custompoolpatterns/README.md** - Quick start guide
4. Run all demos in `custompoolpatterns/`

### Advanced Path
1. **docs/MULTI_HOP_TOUR_EXAMPLE.md** - Complex synchronization
2. Read all pattern-specific READMEs
3. Combine multiple patterns for complex workflows
4. Apply to production applications

---

## 🔗 Cross-References

### From Theory Docs to Code
All theory docs link to relevant package folders:
```markdown
See examples in `completablefuture/` package
```

### From Pattern READMEs to Theory
All pattern READMEs link to relevant theory docs:
```markdown
**Main Theory Document**: [Link to docs/THEORY.md](../../../../docs/THEORY.md)
```

### From Master Index to Everything
`docs/README.md` links to:
- All theory documents
- All use cases
- All pattern packages
- Quick reference tables

---

## 📁 File Locations

### Theory Documents
```
docs/README.md                                    # Master index
docs/COMPLETABLE_FUTURE_THREAD_FLOW_ANALYSIS.md  # CompletableFuture flows
docs/WHY_THREAD_REUSE.md                          # Thread reuse explained
docs/CUSTOM_POOL_PATTERNS_THEORY.md              # Custom pool theory
docs/MULTI_HOP_TOUR_EXAMPLE.md                   # CyclicBarrier example
```

### Pattern READMEs
```
src/main/java/com/shan/concurrency/threadspatterns/completablefuture/README.md
src/main/java/com/shan/concurrency/threadspatterns/custompoolpatterns/README.md
src/main/java/com/shan/concurrency/threadspatterns/cyclicbarrier/README.md
```

### Main Classes (Runnable Demos)
```
src/main/java/com/shan/concurrency/threadspatterns/completablefuture/Main.java
src/main/java/com/shan/concurrency/threadspatterns/custompoolpatterns/Main.java
src/main/java/com/shan/concurrency/threadspatterns/cyclicbarrier/Main.java
[... and all other pattern packages]
```

---

## 🎓 Documentation Principles

### 1. Separation of Concerns
- **Theory docs**: Concepts, deep understanding
- **Pattern READMEs**: Practical usage, running demos

### 2. No Duplication
- Theory written once in `docs/`
- Pattern READMEs link to theory (not duplicate it)

### 3. Progressive Disclosure
- Master index → Brief overview
- Pattern README → Quick start
- Theory docs → Deep dive

### 4. Always Runnable
- Every pattern has a Main class
- Every README shows how to run
- Examples are self-contained

---

## 🚀 Quick Commands

### View Master Index
```bash
cat docs/README.md
```

### View Pattern README
```bash
cat src/main/java/com/shan/concurrency/threadspatterns/completablefuture/README.md
```

### Run Pattern Demo
```bash
mvn exec:java -Dexec.mainClass="com.shan.concurrency.threadspatterns.completablefuture.Main"
```

### Browse All Theory Docs
```bash
ls -la docs/
```

### Browse All Pattern READMEs
```bash
find src/main/java/com/shan/concurrency/threadspatterns -name "README.md"
```

---

## 📈 Benefits of This Structure

### ✅ For Learners
- Clear separation of theory vs practice
- Easy navigation via master index
- Pattern-specific quick starts
- Progressive learning path

### ✅ For Developers
- Theory docs for deep understanding
- Pattern READMEs for quick reference
- Runnable examples in every pattern
- No documentation duplication

### ✅ For Maintainers
- Single source of truth (theory in `docs/`)
- Pattern READMEs focus on usage only
- Easy to add new patterns (follow structure)
- Cross-references prevent drift

---

## 🎯 Next Steps

1. ✅ Start with **docs/README.md** for overview
2. ✅ Pick a pattern based on your needs
3. ✅ Read pattern README for quick start
4. ✅ Read theory docs for deep understanding
5. ✅ Run demos to see concepts in action
6. ✅ Apply to your own projects

---

**Happy Learning! 🚀**
