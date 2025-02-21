package kmp.project.schedule.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kmp.project.schedule.ui.composableItem.CalendarPager
import kmp.project.schedule.util.getOptions
import kmp.project.schedule.util.getRepeat
import kmp.project.schedule.util.timeUtil.convertLocalDateToDate
import kmp.project.schedule.viewModel.RepeatMode
import kmp.project.schedule.viewModel.ScheduleViewModel
import kotlinx.datetime.LocalDate


/**
 * 新建日程页面
 * @param onBack 返回按钮点击事件
 * @param onSave 保存按钮点击事件
 * @param viewModel 新建日程ViewModel
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewSchedule(
    onBack: () -> Unit,
    onSave: () -> Unit,
    viewModel: ScheduleViewModel
) {
    val bringIntoViewRequester = remember {
        BringIntoViewRequester()
    }

    NewScheduleBackHandler(viewModel, onBack)

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
    ) {
        NewScheduleTopBar(
            onBack = onBack,
            onSave = onSave,
            topBarTitle = if (viewModel.id.value == -1) "创建日程" else "编辑日程"
        )
        NewScheduleContent(viewModel, bringIntoViewRequester)
    }
}

/**
 * 新建日程页面顶部栏
 * @param onBack 返回按钮点击事件
 * @param onSave 保存按钮点击事件
 */
@Composable
fun NewScheduleTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    topBarTitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
        ){
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close"
            )
        }

        Text(
            text = topBarTitle,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        IconButton(
            onClick = onSave,
        ){
            Icon(
                imageVector = Icons.Filled.Done,
                contentDescription = "Save"
            )
        }
    }
}

/**
 * 新建日程页面内容
 * @param viewModel 新建日程ViewModel
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewScheduleContent(
    viewModel: ScheduleViewModel,
    bringIntoViewRequester: BringIntoViewRequester
) {
    var title by viewModel.title
    var content by viewModel.content

    LaunchedEffect(content) {
        bringIntoViewRequester.bringIntoView()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 10.dp, top = 10.dp),
    ) {
        item{
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it.substring(0, it.length.coerceAtMost(20))
                },
                label = { Text("事件标题") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp),
                supportingText = {
                    Text(
                        text = "${20 - title.length}",
                        color = if (title.length >= 20) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            DatePickerDocked(viewModel)

            repeatPicker(viewModel)

            locationPicker(viewModel)

            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                },
                label = { Text("备注") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp)
                    .bringIntoViewRequester(bringIntoViewRequester)
            )
        }
    }
}

/**
 * 日期选择器
 * @param viewModel 新建日程ViewModel
 */
@Composable
fun DatePickerDocked(viewModel: ScheduleViewModel) {
    var showDatePickerModal by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        OutlinedTextField(
            value = convertLocalDateToDate(viewModel.date.value),
            onValueChange = { },
            label = { Text("选择日期") },
            readOnly = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        showDatePickerModal = !showDatePickerModal
                    }
                ) {
                    Icon (
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            textStyle = TextStyle(MaterialTheme.colorScheme.onBackground)
        )
        if (showDatePickerModal) {
            DatePickerModal(
                onDismiss = {
                    showDatePickerModal = false
                },
                date = viewModel.date,
            )
        }
    }
}

/**
 * 日期选择器弹窗
 * @param onDismiss 取消按钮点击事件
 * @param date 日期值（时间戳）
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun DatePickerModal(
    onDismiss: () -> Unit,
    date: MutableState<LocalDate>,
) {
    val windowSize = calculateWindowSizeClass()
    val isCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact
    var selectedDate = date.value
    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            modifier = Modifier
                .requiredWidth(if (!isCompact) 500.dp else 380.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(10.dp)
            ) {
                CalendarPager(date.value) { selectedDate = it }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "取消",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                    TextButton(
                        onClick = {
                            date.value = selectedDate
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = "确定",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 重复选择器
 * @param viewModel 新建日程ViewModel
 */
@Composable
fun repeatPicker(
    viewModel: ScheduleViewModel
) {
    var showRepeatPicker by remember { mutableStateOf(false) }
    val selectedRepeat = getRepeat(viewModel.date.value, viewModel.repeatMode.value)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        OutlinedTextField(
            value = selectedRepeat,
            onValueChange = {},
            label = { Text("重复") },
            readOnly = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        showRepeatPicker = !showRepeatPicker
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Select Repeat"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (showRepeatPicker) {
            RepeatPickerModal(
                onDismiss = { showRepeatPicker = false },
                viewModel = viewModel,
                options = getOptions(),
                date = viewModel.date.value
            )
        }
    }
}

/**
 * 重复选择弹窗
 * @param onDismiss 取消按钮点击事件
 * @param viewModel 新建日程ViewModel
 * @param options 重复选项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatPickerModal(
    onDismiss: () -> Unit,
    viewModel: ScheduleViewModel,
    options: List<RepeatMode>,
    date: LocalDate
) {

    //预览选中的重复模式，确认后才将值赋给viewModel中的repeatMode
    val i = remember { mutableStateOf( viewModel.repeatMode.value ) }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp),
            ) {
                Text(
                    text = "重复",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(10.dp)
                )
                options.forEachIndexed { index, repeatMode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, bottom = 5.dp)
                            .clickable {
                                i.value = options[index]
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = i.value == options[index],
                            onClick = { i.value = options[index] }
                        )
                        Text(
                            text = getRepeat(date, repeatMode),
                            fontSize = 15.sp,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "取消",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                    TextButton(
                        onClick = {
                            viewModel.repeatMode.value = i.value
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = "确定",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun locationPicker(
    viewModel: ScheduleViewModel
) {
    var showLocationPicker by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        OutlinedTextField(
            value = viewModel.location.value,
            onValueChange = {},
            readOnly = true,
            label = { Text("地点") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(
                    onClick = {
                        showLocationPicker = !showLocationPicker
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Select Location"
                    )
                }
            }
        )
    }
    if (showLocationPicker) {
        locationPickerModal(
            onDismiss = { showLocationPicker = false },
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun locationPickerModal(
    onDismiss: () -> Unit,
    viewModel: ScheduleViewModel
) {
    val options = listOf("家", "公司", "学校")
    var tempLocation by remember { mutableStateOf(viewModel.location.value) }
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp),
            ) {
                Text(
                    text = "地点",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(10.dp)
                )
                OutlinedTextField(
                    value = tempLocation,
                    onValueChange = {
                        tempLocation = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (option in options) {
                        AssistChip(
                            label = { Text(option, fontWeight = FontWeight.Bold) },
                            onClick = {
                                tempLocation = option
                            },
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "取消",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                    TextButton(
                        onClick = {
                            viewModel.location.value = tempLocation
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = "确定",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
expect fun NewScheduleBackHandler(viewModel: ScheduleViewModel, onBack: () -> Unit)