package util;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Uploads images (courier proof screenshots) to a Supabase Storage bucket.
 * Sprint 3 — US-1.
 *
 * Setup: create a PUBLIC bucket named "courier-proofs" in the Supabase dashboard,
 * then set SUPABASE_URL and SUPABASE_ANON_KEY either as env vars or by editing
 * the constants below.
 */
public final class ImageUploadService {

    // TODO replace these two or set as env vars SUPABASE_URL / SUPABASE_ANON_KEY
    private static final String SUPABASE_URL =
            envOr("SUPABASE_URL", "https://tyzwmebiitvlvorurvip.supabase.co");

    private static final String SUPABASE_ANON_KEY =
            envOr("SUPABASE_ANON_KEY", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InR5endtZWJpaXR2bHZvcnVydmlwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzIyMTMxODksImV4cCI6MjA4Nzc4OTE4OX0.HZt_PAkpl_PEk8qMEctuDyxt5Z3WwU1s94_p2B_Q0Xg"); // (Use the full key you copied earlier)
    private static final String BUCKET = "courier-proofs";

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private ImageUploadService() {}

    /**
     * Uploads the given local file and returns the public URL, or null on failure.
     */
    public static String upload(File file) {
        if (file == null || !file.exists()) return null;
        try {
            String ext = fileExtension(file.getName());
            String key = "proofs/" + UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + key;

            byte[] bytes = Files.readAllBytes(file.toPath());
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Content-Type", guessContentType(ext))
                    .header("x-upsert", "true")
                    .POST(BodyPublishers.ofByteArray(bytes))
                    .build();

            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 == 2) {
                return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/" + key;
            }
            System.err.println("[ImageUploadService] upload failed " + resp.statusCode() + ": " + resp.body());
            return null;
        } catch (Exception e) {
            System.err.println("[ImageUploadService] " + e.getMessage());
            return null;
        }
    }

    private static String fileExtension(String name) {
        int i = name.lastIndexOf('.');
        return (i < 0 || i == name.length() - 1) ? "" : name.substring(i + 1).toLowerCase();
    }

    private static String guessContentType(String ext) {
        return switch (ext) {
            case "png"       -> "image/png";
            case "jpg","jpeg"-> "image/jpeg";
            case "gif"       -> "image/gif";
            case "webp"      -> "image/webp";
            default          -> "application/octet-stream";
        };
    }

    private static String envOr(String name, String fallback) {
        String v = System.getenv(name);
        return (v == null || v.isBlank()) ? fallback : v;
    }
}
