package com.newzic.service

import com.newzic.api.dto.SocialLinksDto
import com.newzic.api.dto.UserResponse
import com.newzic.domain.entity.UserEntity
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toResponse(user: UserEntity): UserResponse {
        return UserResponse(
            id = user.id.toString(),
            username = user.username,
            email = user.email,
            displayName = user.displayName,
            avatar = user.avatar,
            cover = user.cover,
            bio = user.bio,
            longBio = user.longBio,
            roles = user.roles.map { it.name.lowercase() },
            followers = user.followers,
            following = user.following,
            totalPlays = user.totalPlays,
            genres = user.genres.toList(),
            tags = user.tags.toList(),
            verified = user.verified,
            premium = user.premium,
            country = user.country,
            location = user.location,
            preferredGenres = user.preferredGenres.toList(),
            lookingForCollab = user.lookingForCollab,
            collabDescription = user.collabDescription,
            weeklyGrowth = user.weeklyGrowth,
            socialLinks = SocialLinksDto(
                spotify = user.spotifyUrl,
                youtubeMusic = user.youtubeMusicUrl,
                appleMusic = user.appleMusicUrl,
                soundcloud = user.soundcloudUrl,
                tiktok = user.tiktokUrl,
                instagram = user.instagramUrl
            ),
            photos = user.photos.toList(),
            joinedDate = user.joinedDate.toString()
        )
    }
}
