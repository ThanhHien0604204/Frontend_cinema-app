package com.ntth.movie_ticket_booking_app.utils;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Bắt buộc có để Glide tạo GeneratedAppGlideModuleImpl.
 * Không cần override gì thêm. Có thể để trống như dưới.
 */
@GlideModule
public final class MyAppGlideModule extends AppGlideModule {
    // Tuỳ chọn: tắt manifest parsing cho nhanh build (không bắt buộc)
    // @Override
    // public boolean isManifestParsingEnabled() { return false; }
}
