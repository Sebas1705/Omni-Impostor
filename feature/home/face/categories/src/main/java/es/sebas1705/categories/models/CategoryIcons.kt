package es.sebas1705.categories.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NoAdultContent
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.vector.ImageVector
import es.sebas1705.models.Categories

/** One distinct icon per category; `CategoryIconsTest` fails if two categories share one. */
@Suppress("CyclomaticComplexMethod")
internal fun Categories.toCategoryIcon(): ImageVector = when (this) {
    Categories.ANIMALS -> Icons.Outlined.Pets
    Categories.FOOD_AND_DRINKS -> Icons.Outlined.Fastfood
    Categories.SPORTS -> Icons.Outlined.SportsSoccer
    Categories.MUSIC -> Icons.Outlined.MusicNote
    Categories.MOVIES_AND_SERIES -> Icons.Outlined.Movie
    Categories.ANIME -> Icons.Outlined.Tv
    Categories.VIDEO_GAMES -> Icons.Outlined.SportsEsports
    Categories.CELEBRITIES -> Icons.Outlined.Stars
    Categories.SOCIAL_MEDIA_AND_STREAMERS -> Icons.Outlined.Videocam
    Categories.INTERNET_AND_MEMES -> Icons.Outlined.EmojiEmotions
    Categories.BRANDS -> Icons.Outlined.Sell
    Categories.STORES_AND_RETAIL -> Icons.Outlined.Storefront
    Categories.APPS_AND_PLATFORMS -> Icons.Outlined.Apps
    Categories.CARS_AND_MOTORSPORT -> Icons.Outlined.DirectionsCar
    Categories.CITIES_AND_LANDMARKS -> Icons.Outlined.LocationCity
    Categories.ADULT_18_PLUS -> Icons.Outlined.NoAdultContent
    Categories.TECHNOLOGY -> Icons.Outlined.Computer
    Categories.SCIENCE_PHYSICS -> Icons.Outlined.Bolt
    Categories.SCIENCE_CHEMISTRY -> Icons.Outlined.Science
    Categories.SCIENCE_BIOLOGY -> Icons.Outlined.Biotech
    Categories.SPACE_AND_ASTRONOMY -> Icons.Outlined.RocketLaunch
    Categories.HISTORY -> Icons.Outlined.HistoryEdu
    Categories.GEOGRAPHY -> Icons.Outlined.Public
    Categories.TRANSPORT -> Icons.Outlined.Train
    Categories.PROFESSIONS -> Icons.Outlined.Work
    Categories.SCHOOL_AND_EDUCATION -> Icons.Outlined.School
    Categories.HEALTH_AND_MEDICINE -> Icons.Outlined.MedicalServices
    Categories.FASHION_AND_CLOTHING -> Icons.Outlined.Checkroom
    Categories.HOME_AND_FURNITURE -> Icons.Outlined.Chair
    Categories.NATURE_AND_WEATHER -> Icons.Outlined.Landscape
    Categories.HOLIDAYS_AND_CELEBRATIONS -> Icons.Outlined.Celebration
    Categories.MYTHOLOGY_AND_FANTASY -> Icons.Outlined.AutoAwesome
    Categories.BOOKS_AND_LITERATURE -> Icons.Outlined.Book
    Categories.ART_AND_DESIGN -> Icons.Outlined.Palette
    Categories.BUSINESS_AND_FINANCE -> Icons.Outlined.AccountBalance
    Categories.POLITICS_AND_LAW -> Icons.Outlined.Gavel
    Categories.CULTURE_AND_TRADITIONS -> Icons.Outlined.Groups
    Categories.TRAVEL_AND_TOURISM -> Icons.Outlined.FlightTakeoff
    Categories.SUPERHEROES_AND_COMICS -> Icons.Outlined.Shield
    Categories.BOARD_GAMES_AND_TABLETOP -> Icons.Outlined.Extension
    Categories.KITCHEN_AND_COOKWARE -> Icons.Outlined.Kitchen
    Categories.OFFICE_AND_STATIONERY -> Icons.Outlined.Description
    Categories.EMOTIONS_AND_FEELINGS -> Icons.Outlined.Mood
    Categories.VALUES_AND_ETHICS -> Icons.Outlined.Balance
}
