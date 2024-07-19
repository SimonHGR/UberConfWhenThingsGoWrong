package eitherstuff.Example;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

interface ExFunction<A, R> {
  R apply(A a) throws Throwable;


  public static <A, R> Function<A, Either<R, Throwable>> wrap(ExFunction<A, R> fn) {
    return a -> {
      try {
        return Either.success(fn.apply(a));
      } catch (Throwable t) {
        return Either.failure(t);
      }
    };
  }
}

class Either<S, F> {
  private S success;
  private F failure;

  private Either(S s, F f) {
    success = s;
    failure = f;
  }

  public static <S, F> Either<S, F> success(S s) {
    return new Either<>(s, null);
  }

  public static <S, F> Either<S, F> failure(F f) {
    return new Either<>(null, f);
  }

  public boolean isSuccess() {
    return failure == null;
  }

  public boolean isFailure() {
    return failure != null;
  }

  public S getSuccess() {
    if (isFailure()) throw new IllegalStateException("XXX");
    return success;
  }

  public F getFailure() {
    if (isSuccess()) throw new IllegalStateException("yyyy");
    return failure;
  }

  public void report(Consumer<F> op) {
    if (isFailure()) {
      op.accept(failure);
    }
  }

  public Either<S, F> reportUnary(Consumer<F> op) {
    if (isFailure()) {
      op.accept(failure);
    }
    return this;
  }

  public Either<S, F> mapIfFailure(UnaryOperator<Either<S, F>> op) {
    if (isFailure()) {
      return op.apply(this);
    } else return this;
  }

  public static <S, F> UnaryOperator<Either<S, F>> retries(UnaryOperator<Either<S, F>>... ops) {
    return e -> {
//      Either<S, F> self = e;
      for (var op : ops) {
        if (e.isSuccess()) break;
        e = op.apply(e);
      }
      return e;
    };
  }
}

public class Example {

  public static void main(String[] args) {
    Function<String, Either<Stream<String>, Throwable>> getLines =
        ExFunction.wrap(fn -> Files.lines(Path.of(fn)));

    Map<String, String> fallbackFiles = Map.of(
        "b.txt", "d.txt"
    );

    UnaryOperator<Either<Stream<String>, Throwable>> recoverFile =
        e -> {
          String failedFile = e.getFailure().getMessage();
          String recoveryFile = fallbackFiles.get(failedFile);
          return getLines.apply(recoveryFile);
        };

    UnaryOperator<Either<Stream<String>, Throwable>> delay = e -> {
      try {
        Thread.sleep(1500);
      } catch (InterruptedException ie) {
        System.out.println("Uh oh, interrupt!");
      }
      return e;
    };


    UnaryOperator<Either<Stream<String>, Throwable>> retryFile =
        e -> {
          if (e.isFailure()) {
            String failedFile = e.getFailure().getMessage();
            return getLines.apply(failedFile);
          } else return e;
        };

    Stream.of("a.txt", "b.txt", "c.txt")
        .map(getLines)
//        .peek(e -> {
//          if (e.isFailure()) {
//            System.err.println("File not found " + e.getFailure().getMessage());
//          }
//        })
//        .peek(e -> e.report(System.out::println))
//        .map(e -> e.mapIfFailure(delay))
//        .map(retryFile)
//        .peek(e -> e.report(System.out::println))
//        .map(e -> e.mapIfFailure(recoverFile))
//        .peek(e -> e.report(System.out::println))
        .map(Either.retries(
                e -> e.reportUnary(System.out::println),
                e -> e.mapIfFailure(delay),
                retryFile,
                e -> e.reportUnary(System.out::println),
                e -> e.mapIfFailure(recoverFile),
                e -> e.reportUnary(System.out::println)))
            .filter(Either::isSuccess)
            .flatMap(Either::getSuccess)
            .forEach(System.out::println);
  }
}
