package com.newzic.service

import com.newzic.api.dto.ReactionsDto
import com.newzic.api.dto.SongResponse
import com.newzic.domain.entity.SongEntity
import org.springframework.stereotype.Component

@Component
class SongMapper {

    fun toResponse(song: SongEntity): SongResponse {
        return SongResponse(
            id = song.id.toString(),
            title = song.title,
            artistId = song.artist.id.toString(),
            artistName = song.artist.displayName,
            artistAvatar = song.artist.avatar,
            albumId = song.album?.id?.toString(),
            albumName = song.album?.title,
            cover = song.cover,
            duration = song.duration,
            genre = song.genre,
            tags = song.tags.toList(),
            releaseDate = song.releaseDate.toString() + "Z",
            plays = song.plays,
            likes = song.likes,
            reactions = ReactionsDto(
                fire = song.reactionsFire,
                gem = song.reactionsGem,
                onpoint = song.reactionsOnpoint,
                star = song.reactionsStar
            ),
            audioUrl = song.audioUrl,
            isExplicit = song.isExplicit
        )
    }
}
