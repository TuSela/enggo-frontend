package com.nhom12.enggo_backend.repository.exam;

import com.nhom12.enggo_backend.entity.exam.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Integer> {
    boolean existsByThemeName(String themeName);;
    List<Theme> findByCategory(String category);
    boolean existsByCategory(String category);
    Theme findByThemeName(String themeName);
}
