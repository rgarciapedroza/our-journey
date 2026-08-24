package com.ourjourney.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String url;

    @Value("${supabase.key}")
    private String key;

    @Value("${supabase.bucket}")
    private String bucket;

    @Value("${supabase.trip-photos-bucket}")
    private String tripPhotosBucket;
}