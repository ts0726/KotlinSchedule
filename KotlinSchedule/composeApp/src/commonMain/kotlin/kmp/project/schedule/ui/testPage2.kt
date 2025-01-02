package kmp.project.schedule.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TestPage2() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val testList = remember { mutableStateListOf("1", "2", "3") }
        Row {
            Text(
                text = "Test Page 2",
//                modifier = Modifier
//                    .fillMaxSize(),
                textAlign = TextAlign.Center
            )
            Button(onClick = {
                testList.add("New Item")
                println("testList: $testList")
            }) {
                Text(text = "Add Item")
            }
            Button(onClick = {
                testList.removeAt(0)
                println("testList: $testList")
            }) {
                Text(text = "Remove Item")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
//            items(testList.size, ) { index ->
//                Text(text = testList[index],
//                    modifier = Modifier.animateItemPlacement()
//                )
//            }
            items(count = testList.size, key = { index -> testList[index] }) { index ->
                Text(
                    text = testList[index],
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }

}