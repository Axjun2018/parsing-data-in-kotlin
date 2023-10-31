package com.example.littlelemon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.littlelemon.ui.theme.LittleLemonTheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    //1. Create an HttpClient instance
    private val client = HttpClient(Android) { // Pass in the Android engine
        install(ContentNegotiation) { //  install the ContentNegotiation plugin
            //Configure it to use JSON
            //the JSON file is returned as plain text, set the content type to text/plain.
            json(contentType = ContentType("text", "plain"))
        }
    }

    //2. Define menuItemsLiveData
    //In order to have a dynamic list of menu items that can be updated once the data is fetched from the API
    private val menuItemsLiveData = MutableLiveData<List<String>>()

    //3. Define getMenu method
    // To fetch the menu from the API, it takes a category as an input and returns a list of menu items.
    private suspend fun getMenu(category: String) : List<String>{
        val response: Map<String, MenuCategory> =
            client.get("https://raw.githubusercontent.com/Meta-Mobile-Developer-PC/Working-With-Data-API/main/littleLemonMenu.json")
                .body() // return body, then we can retrieve details from JSON data
        return response[category]?.menu?: listOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //4. Retrieve menu items from network
        lifecycleScope.launch {
            val menuItems = getMenu("Salads")
            runOnUiThread {
                menuItemsLiveData.value = menuItems
            }
        }
        setContent {
            LittleLemonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Display menu items
                        // initially set state as an emptyList
                        // by: assign state value to items, no need .value
                        // = : assign state, need .value to convert
                        val items by menuItemsLiveData.observeAsState(emptyList())
                        // display
                        MenuItems(items)
//                        val items = menuItemsLiveData.observeAsState(emptyList())
//                        // display
//                        MenuItems(items.value)
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItems(
    items: List<String> = emptyList(), // set default value as an emptyList()
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        LazyColumn {
            itemsIndexed(items) { _, item ->
                MenuItemDetails(item)
            }
        }
    }
}

@Composable
fun MenuItemDetails(menuItem: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = menuItem)
    }
}
