package models

data class FavoriteItemDetails(var item_name: String? = null,
                               var item_image: String? = null,
                               var item_uid: String? = null,
                               var item_price: String? = null,
                               var specification: String? = null,
                               var discount: String? = null,
                               var category: String? = null,
)