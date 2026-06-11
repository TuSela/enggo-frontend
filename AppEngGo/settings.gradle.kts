pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    // 🎯 Giữ nguyên chế độ quản lý tập trung bảo mật
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // 🎯 BẮT BUỘC THÊM DÒNG NÀY: Để tải được thư viện STOMP WebSocket (NaikSoftware) và các thư viện Android mở rộng khác
        maven { url = java.net.URI("https://jitpack.io") }
    }
}
rootProject.name = "AppEngGo"
include(":app")