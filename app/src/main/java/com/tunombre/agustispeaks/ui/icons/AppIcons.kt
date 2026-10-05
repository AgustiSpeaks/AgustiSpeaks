package com.tunombre.agustispeaks.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Bathroom
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildFriendly
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DoNotTouch
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.KebabDining
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Wash
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Traduce el nombre lógico de icono guardado en el modelo de datos
 * (`Place.icon`, `Category.icon`, `Pictogram.icon`) a un [ImageVector].
 * Si el nombre no está mapeado devuelve un icono genérico.
 *
 * Más adelante estos iconos se sustituirán por pictogramas ilustrados
 * (modo edición / personalización) sin tocar la capa de datos.
 */
fun appIcon(name: String): ImageVector = when (name) {
    "ac_unit" -> Icons.Filled.AcUnit
    "accessibility_new" -> Icons.Filled.AccessibilityNew
    "add" -> Icons.Filled.Add
    "add_circle" -> Icons.Filled.AddCircle
    "auto_stories" -> Icons.Filled.AutoStories
    "back_hand" -> Icons.Filled.BackHand
    "backpack" -> Icons.Filled.Backpack
    "bakery_dining" -> Icons.Filled.BakeryDining
    "bathroom" -> Icons.Filled.Bathroom
    "bathtub" -> Icons.Filled.Bathtub
    "bed" -> Icons.Filled.Bed
    "bedtime" -> Icons.Filled.Bedtime
    "brush" -> Icons.Filled.Brush
    "cake" -> Icons.Filled.Cake
    "chair" -> Icons.Filled.Chair
    "check" -> Icons.Filled.Check
    "check_circle" -> Icons.Filled.CheckCircle
    "checkroom" -> Icons.Filled.Checkroom
    "child_friendly" -> Icons.Filled.ChildFriendly
    "clean_hands" -> Icons.Filled.CleanHands
    "close" -> Icons.Filled.Close
    "cloud" -> Icons.Filled.Cloud
    "cookie" -> Icons.Filled.Cookie
    "directions_car" -> Icons.Filled.DirectionsCar
    "directions_run" -> Icons.Filled.DirectionsRun
    "directions_walk" -> Icons.Filled.DirectionsWalk
    "do_not_touch" -> Icons.Filled.DoNotTouch
    "draw" -> Icons.Filled.Draw
    "edit" -> Icons.Filled.Edit
    "egg" -> Icons.Filled.Egg
    "elderly" -> Icons.Filled.Elderly
    "emoji_food_beverage" -> Icons.Filled.EmojiFoodBeverage
    "event_seat" -> Icons.Filled.EventSeat
    "face" -> Icons.Filled.Face
    "family_restroom" -> Icons.Filled.FamilyRestroom
    "favorite" -> Icons.Filled.Favorite
    "healing" -> Icons.Filled.Healing
    "hearing" -> Icons.Filled.Hearing
    "heart_broken" -> Icons.Filled.HeartBroken
    "help" -> Icons.Filled.Help
    "help_outline" -> Icons.Filled.HelpOutline
    "history" -> Icons.Filled.History
    "home" -> Icons.Filled.Home
    "hourglass_empty" -> Icons.Filled.HourglassEmpty
    "icecream" -> Icons.Filled.Icecream
    "kebab_dining" -> Icons.Filled.KebabDining
    "local_bar" -> Icons.Filled.LocalBar
    "local_cafe" -> Icons.Filled.LocalCafe
    "local_drink" -> Icons.Filled.LocalDrink
    "local_hospital" -> Icons.Filled.LocalHospital
    "lock" -> Icons.Filled.Lock
    "lock_open" -> Icons.Filled.LockOpen
    "login" -> Icons.Filled.Login
    "logout" -> Icons.Filled.Logout
    "lunch_dining" -> Icons.Filled.LunchDining
    "medication" -> Icons.Filled.Medication
    "menu_book" -> Icons.Filled.MenuBook
    "monitor_heart" -> Icons.Filled.MonitorHeart
    "mood" -> Icons.Filled.Mood
    "mood_bad" -> Icons.Filled.MoodBad
    "music_note" -> Icons.Filled.MusicNote
    "nino_jugando" -> NinoJugando
    "nutrition" -> Icons.Filled.Restaurant
    "paid" -> Icons.Filled.Paid
    "palette" -> Icons.Filled.Palette
    "pan_tool" -> Icons.Filled.PanTool
    "park" -> Icons.Filled.Park
    "pause" -> Icons.Filled.Pause
    "payments" -> Icons.Filled.Payments
    "people" -> Icons.Filled.People
    "person" -> Icons.Filled.Person
    "place" -> Icons.Filled.Place
    "psychology" -> Icons.Filled.Psychology
    "ramen_dining" -> Icons.Filled.RamenDining
    "remove_circle" -> Icons.Filled.RemoveCircle
    "restaurant" -> Icons.Filled.Restaurant
    "rice_bowl" -> Icons.Filled.RiceBowl
    "schedule" -> Icons.Filled.Schedule
    "school" -> Icons.Filled.School
    "search" -> Icons.Filled.Search
    "self_improvement" -> Icons.Filled.SelfImprovement
    "sentiment_dissatisfied" -> Icons.Filled.SentimentDissatisfied
    "sentiment_neutral" -> Icons.Filled.SentimentNeutral
    "sentiment_satisfied" -> Icons.Filled.SentimentSatisfied
    "sentiment_very_dissatisfied" -> Icons.Filled.SentimentVeryDissatisfied
    "sentiment_very_satisfied" -> Icons.Filled.SentimentVerySatisfied
    "settings" -> Icons.Filled.Settings
    "shopping_bag" -> Icons.Filled.ShoppingBag
    "shopping_basket" -> Icons.Filled.ShoppingBasket
    "shopping_cart" -> Icons.Filled.ShoppingCart
    "shower" -> Icons.Filled.Shower
    "sick" -> Icons.Filled.Sick
    "soup_kitchen" -> Icons.Filled.SoupKitchen
    "sports_esports" -> Icons.Filled.SportsEsports
    "sports_soccer" -> Icons.Filled.SportsSoccer
    "star" -> Icons.Filled.Star
    "stop" -> Icons.Filled.Stop
    "table_restaurant" -> Icons.Filled.TableRestaurant
    "thermostat" -> Icons.Filled.Thermostat
    "thumb_down" -> Icons.Filled.ThumbDown
    "thumb_up" -> Icons.Filled.ThumbUp
    "today" -> Icons.Filled.Today
    "touch_app" -> Icons.Filled.TouchApp
    "toys" -> Icons.Filled.Toys
    "vaccines" -> Icons.Filled.Vaccines
    "visibility" -> Icons.Filled.Visibility
    "volunteer_activism" -> Icons.Filled.VolunteerActivism
    "wash" -> Icons.Filled.Wash
    "water_drop" -> Icons.Filled.WaterDrop
    "waving_hand" -> Icons.Filled.WavingHand
    "wb_sunny" -> Icons.Filled.WbSunny
    "wc" -> Icons.Filled.Wc
    "weekend" -> Icons.Filled.Weekend
    "whatshot" -> Icons.Filled.Whatshot
    else -> Icons.Filled.Category
}

/**
 * Dibuja el ícono de nombre [name], teñido con el color del texto que lo rodea.
 * Todos los íconos de la app son de un solo color, incluidos los dibujados a
 * mano como [NinoJugando]: así la estética es pareja en todas las tarjetas.
 */
@Composable
fun AppIcon(
    name: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Icon(
        imageVector = appIcon(name),
        contentDescription = contentDescription,
        modifier = modifier
    )
}
