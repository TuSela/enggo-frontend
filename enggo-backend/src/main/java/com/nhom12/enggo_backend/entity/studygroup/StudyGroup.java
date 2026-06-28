package com.nhom12.enggo_backend.entity.studygroup;

import com.nhom12.enggo_backend.entity.identity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "study_groups")
public class StudyGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "group_name", nullable = false, length = 150)
    String groupName;

    @Column(columnDefinition = "text")
    String description;

    @Column(name = "group_mode", length = 50)
    String groupMode;

    @Column(name = "invite_link", unique = true, length = 255)
    String inviteLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    User creator;

    @Column(name = "avatar_url", length = 255)
    String avatarUrl;

    @Column(name = "member_count")
    Integer memberCount;

    @Column(name = "post_count")
    Integer postCount;

    @Column(name = "created_at", insertable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    LocalDateTime updatedAt;
}
