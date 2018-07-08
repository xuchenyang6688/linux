package java8;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Created by Sharon on 2018/7/8.
 * Note: https://www.jianshu.com/p/4897ccdcb278
 */
public class TestCompleteFuture {
    ThreadPoolExecutor executor;
    Consumer consumer;

    /**
     * complete
     * completeExceptionally
     * get
     */
    public void testBasicCompleteFuture() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println();
                    completableFuture.complete("OK");
                } catch (Exception e) {
                    completableFuture.completeExceptionally(e);
                }
            }
        };
        try {
            completableFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * supplyAsync(function, executor)
     * thenAccept
     */
    public void testCompleteFuture() {
        CompletableFuture.supplyAsync(() -> {
            try {
                //trigger();
                return "OK";
            } catch (Exception e) {
                return e.getMessage();
            }
        }, executor).thenAccept(result -> {
            System.out.println("Complete");
        });
    }

    public void testMultiCompleteFuture() {
        // ------------------------------- all of -------------------------
        CompletableFuture<String> completableFuture1 = CompletableFuture.supplyAsync(() -> {
            return "result1";
        });
        CompletableFuture completableFuture2 = CompletableFuture.supplyAsync(() -> {
            return "result2";
        });
        CompletableFuture<Void> allResult = CompletableFuture.allOf(completableFuture1, completableFuture2);
        // wait all
        allResult.join();

        // ------------------------------- any of ------------------------------
        CompletableFuture anyResult = CompletableFuture.anyOf(completableFuture1, completableFuture2);

        // ------------------------------- then compose the second use the first's result--------------------------
        CompletableFuture<String> c = completableFuture1.thenCompose(result1 ->
                CompletableFuture.supplyAsync(() -> {
                    return result1 + "result2";
                })
        );
        try {
            c.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ------------------------------- then combine independent but use all result--------------------------
        completableFuture1.thenCombine(completableFuture2, (result1, result2) -> {
            return result1 + result2;
        });

    }


    //1. Future vs Complete Future -> Why use complete future
    //   ----------------- Runnable run method has no return and exception ---------------------------

    /**
     * 1. New Thread or Extend Thread
     * 2. Override run
     * 3. Thread.start()
     */
    public void createThread() {
        Thread thread1 = new Thread();
        Thread thread = new Thread() {
            @Override
            public void run() {
                System.out.println("Thread");
            }
        };
        thread.start();
    }

    /**
     * 1. Implement Runnbale
     * 2. Override run
     * 3. New Thread(Runnable).start();
     */
    public void createRunnable() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                System.out.println("Runnable");
            }
        };
        new Thread(r).start();
        // lambda
        Runnable r1 = () -> {
            System.out.println("Runnable lambda");
        };
        new Thread(r1).start();

    }

    // ------------------------- Callable call method can return and throws exception -------------------
    public void createCallable() {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return null;
            }
        };
        FutureTask<String> futureTask = new FutureTask<>(callable);
        new Thread(futureTask).start();
    }


    // ------------------ ThreadPool has return - Future ---------------------------------

    /**
     * Collect thread pool's result by the task completion sequence
     * https://www.cnblogs.com/dennyzhangdd/p/7010972.html#_label3
     */
    public void useFuture() {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Future<String> future = executor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    throw new Exception("call throw exception");
                }
            });
            futures.add(future);
        }
        futures.forEach(future -> {
            if (future.isDone() || future.isCancelled()) {
                try {
                    System.out.println(future.get());
                } catch (Exception e) {
//                    e.printStackTrace();
                    System.out.println("exception");
                }
            }
            ;

        });
    }

    public static void main(String[] args) {
        new TestCompleteFuture().useFuture();
    }


}
