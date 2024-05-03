package ru.shotin.tasks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class Task3ChainedExceptionsComplex {
    static List<Supplier<Exception>> rootCauseSuppliers = Arrays.asList(
            () -> new NullPointerException("root exception"),
            () -> new NumberFormatException("root exception"),
            () -> new IllegalCharsetNameException("root exception"),
            () -> new FileNotFoundException("root exception"),
            () -> new AccessDeniedException("root exception"),
            () -> new ArithmeticException("root exception"),
            () -> null
    );

    static List<Function<Exception, Exception>> exFunctions = Arrays.asList(
            (cause) -> new Exception("exception", cause),
            (cause) -> new RuntimeException("runtime exception", cause),
            (cause) -> new IllegalArgumentException("illegal argument", cause),
            (cause) -> new IllegalStateException("illegal state", cause),
            (cause) -> new IOException("uncategorized I/O problem", cause)
    );

    static final Random rnd = new Random(System.nanoTime());

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            HeavyResource heavyResource = new HeavyResource();
            try {
                heavyResource.importantMethod();
            } catch (Exception e) {
                if (e instanceof IOException) { // TODO: исправить если содержит IOException в цепочке
                    System.err.println("=== Caused by input or output problem ===");
                } else if (e instanceof IllegalArgumentException) { // TODO: исправить если содержит IllegalArgumentException в цепочке
                    System.err.println("=== Caused by some illegal argument ===");
                } else {
                    System.err.println("=== Caused by root ===" + e);
                }
                e.printStackTrace();
                System.err.println();
            }
        }
        System.out.println("=== exit ===");
    }

    static boolean hasCause(Throwable source, Class<?> classToFind) {
        return false;
    }

    static Throwable findRootCause(Throwable source) {
        return source;
    }

    static class HeavyResource implements AutoCloseable {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

        {
            executorService.scheduleAtFixedRate(() -> {
            }, 0, 1, TimeUnit.SECONDS);
        }

        void importantMethod() throws Exception {
            Exception exception = null;
            exception = rootCauseSuppliers.get(rnd.nextInt(rootCauseSuppliers.size())).get();
            if (exception != null) {
                for (int i = 0; i < rnd.nextInt(exFunctions.size()); i++) {
                    exception = exFunctions.get(rnd.nextInt(exFunctions.size())).apply(exception);
                }
            }
            if (exception != null) {
                throw exception;
            }
        }

        @Override
        public void close() throws Exception {
            executorService.shutdownNow();
        }
    }
}
