package streamerrors;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
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
}

public class Example {
////  public static Stream<String> getLines(String fn) {
//  public static Optional<Stream<String>> getLines(String fn) {
//    try {
//      return Optional.of(Files.lines(Path.of(fn)));
//    } catch (IOException e) {
////      throw new RuntimeException(e);
//      System.err.println("file " + e.getMessage() + " not found");
//      return Optional.empty();
//    }
//  }

//  public static Either<Stream<String>, Throwable> getLines(String fn) {
//    try {
//      return Either.success(Files.lines(Path.of(fn)));
//    } catch (IOException e) {
//      System.err.println("file " + e.getMessage() + " not found");
//      return Either.failure(e);
//    }
//  }

  public static void main(String[] args) {
    Function<String, Either<Stream<String>, Throwable>> getLines =
        ExFunction.wrap(fn -> Files.lines(Path.of(fn)));

    Stream.of("a.txt", "b.txt", "c.txt")
//        .flatMap(Example::getLines)
//        .map(Example::getLines)
        .map(getLines)
//        .peek(opt -> {
//          if(opt.isEmpty()) {
//            System.out.println("Uh oh, something wasn't found");
//          }
//        })
        .peek(e -> {
          if (e.isFailure()) {
            System.err.println("File not found " + e.getFailure().getMessage());
          }
        })
        .filter(Either::isSuccess)
        .flatMap(Either::getSuccess)
        .forEach(System.out::println);
  }
}
