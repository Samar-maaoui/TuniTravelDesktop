package services;

import com.cloudinary.Cloudinary;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryInitializer {

    private static Cloudinary cloudinaryInstance;

    public static Cloudinary getInstance() {
        if (cloudinaryInstance == null) {
            Map<String, String> config = new HashMap<>();
            //Retrieve from environment variables (best practice for security)
            config.put("cloud_name", System.getenv("CLOUDINARY_CLOUD_NAME"));
            config.put("api_key", System.getenv("CLOUDINARY_API_KEY"));
            config.put("api_secret", System.getenv("CLOUDINARY_API_SECRET"));

            //Handle missing environment variables - this should only be for development/testing!
            if (config.get("cloud_name") == null || config.get("api_key") == null || config.get("api_secret") == null) {
                System.err.println("WARNING: Cloudinary API keys not found in environment variables. Using hardcoded keys. This is a security risk!");
                config.put("cloud_name", "dihvo7nh9"); // Replace with your cloud name if environment variables are not set
                config.put("api_key", "559899894249619"); // Replace with your API key
                config.put("api_secret", "pM6yFsHtC7qjzIbU5-psns7sz3o"); // Replace with your API secret
            }


            try {
                cloudinaryInstance = new Cloudinary(config);
                System.out.println("Cloudinary initialized successfully.");
            } catch (Exception e) {
                System.err.println("Error initializing Cloudinary: " + e.getMessage());
                e.printStackTrace(); // Print stack trace for debugging
                //Handle the error appropriately - maybe throw an exception, exit the app, or use a fallback mechanism
            }
        }
        return cloudinaryInstance;
    }
}