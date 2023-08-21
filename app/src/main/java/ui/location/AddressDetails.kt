package ui.location

data class AddressDetails(var address_name: String? = null,
                          var address_mobile: String? = null,
                          var address_line1: String? = null,
                          var address_line2: String? = null,
                          var address_city: String? = null,
                          var address_pincode: String? = null,
                          var address_state: String? = null,
                          var address_type: String? = null,
                          var address_country: String? = null,
                          var address_id: String?=null,
                          var isChecked: Boolean = false)