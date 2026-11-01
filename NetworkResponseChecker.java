import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * A small command-line tool that checks HTTP response times for a URL.
 * Requires Java 11 or later.
 */
public class NetworkResponseChecker {
    private static final int DEFAULT_ATTEMPTS = 10;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=== 通信応答時間チェッカー ===");
            System.out.println("指定したWebサイトに複数回アクセスし、応答時間を測定します。");
            System.out.print("測定するURL（例：https://www.playstation.com/ja-jp/）：");
            String inputUrl = scanner.nextLine().trim();

            if (inputUrl.isEmpty()) {
                System.out.println("URLが入力されていません。終了します。");
                return;
            }

            URI uri = createUri(inputUrl);
            if (uri == null) {
                System.out.println("URLの形式を確認してください。http:// または https:// で始まるURLを入力してください。");
                return;
            }

            System.out.print("測定回数（未入力の場合は10回）：");
            String attemptsText = scanner.nextLine().trim();
            int attempts = DEFAULT_ATTEMPTS;
            if (!attemptsText.isEmpty()) {
                try {
                    attempts = Integer.parseInt(attemptsText);
                } catch (NumberFormatException e) {
                    System.out.println("測定回数は整数で入力してください。");
                    return;
                }
            }
            if (attempts < 1 || attempts > 100) {
                System.out.println("測定回数は1～100回で指定してください。");
                return;
            }

            runMeasurements(uri, attempts);
        } catch (Exception e) {
            System.out.println("予期しないエラーが発生しました：" + e.getMessage());
        }
    }

    private static URI createUri(String input) {
        String normalized = input;
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        try {
            URI uri = URI.create(normalized);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || !("http".equalsIgnoreCase(scheme)
                    || "https".equalsIgnoreCase(scheme))) {
                return null;
            }
            return uri;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void runMeasurements(URI uri, int attempts) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        List<Long> successfulTimes = new ArrayList<>();
        int successfulResponses = 0;
        int unsuccessfulStatuses = 0;
        int connectionErrors = 0;

        System.out.println();
        System.out.println("測定先：" + uri);
        System.out.println("測定回数：" + attempts + "回");
        System.out.println("----------------------------------------");

        for (int i = 1; i <= attempts; i++) {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "NetworkResponseChecker/1.0")
                    .GET()
                    .build();

            long start = System.nanoTime();
            try {
                HttpResponse<Void> response = client.send(
                        request, HttpResponse.BodyHandlers.discarding());
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                int status = response.statusCode();

                if (status >= 200 && status < 300) {
                    successfulResponses++;
                    successfulTimes.add(elapsedMs);
                    System.out.printf(Locale.JAPAN, "%2d回目：成功  HTTP %d  応答時間 %d ms%n",
                            i, status, elapsedMs);
                } else {
                    unsuccessfulStatuses++;
                    System.out.printf(Locale.JAPAN, "%2d回目：HTTPステータス %d  応答時間 %d ms%n",
                            i, status, elapsedMs);
                }
            } catch (Exception e) {
                connectionErrors++;
                System.out.printf(Locale.JAPAN, "%2d回目：接続エラー（%s）%n",
                        i, e.getClass().getSimpleName());
            }
        }

        printSummary(attempts, successfulResponses, unsuccessfulStatuses,
                connectionErrors, successfulTimes);
    }

    private static void printSummary(int attempts, int success, int badStatus,
                                     int errors, List<Long> times) {
        System.out.println("----------------------------------------");
        System.out.println("測定結果");
        System.out.printf(Locale.JAPAN, "HTTP 2xx成功：%d / %d回%n", success, attempts);
        System.out.printf(Locale.JAPAN, "その他のHTTPステータス：%d回%n", badStatus);
        System.out.printf(Locale.JAPAN, "接続エラー：%d回%n", errors);
        System.out.printf(Locale.JAPAN, "HTTP 2xx成功率：%.1f%%%n", success * 100.0 / attempts);

        if (!times.isEmpty()) {
            long min = times.stream().mapToLong(Long::longValue).min().orElse(0);
            long max = times.stream().mapToLong(Long::longValue).max().orElse(0);
            double average = times.stream().mapToLong(Long::longValue).average().orElse(0);
            System.out.printf(Locale.JAPAN, "最短応答時間：%d ms%n", min);
            System.out.printf(Locale.JAPAN, "最長応答時間：%d ms%n", max);
            System.out.printf(Locale.JAPAN, "平均応答時間：%.1f ms%n", average);
        } else {
            System.out.println("成功した応答がないため、応答時間の統計は表示できません。");
        }

        System.out.println();
        System.out.println("※このツールはWebサイトへのHTTP応答時間を測定するものです。");
        System.out.println("  通信速度、動画の再生品質、PlayStation Portalのストリーミング品質を直接測定するものではありません。");
    }
}
