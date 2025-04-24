package com.hajunho.istf_storage

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.hajunho.istf_storage.ui.theme.Istf_storageTheme
import java.io.File
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {
    private var storageData = mutableStateOf<StorageInfo?>(null)

    // 권한 요청 결과 처리
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                updateStorageInfo()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 권한 확인 및 요청
        checkPermission()

        setContent {
            Istf_storageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StorageInfoScreen(
                        storageInfo = storageData.value,
                        onRefreshClick = { updateStorageInfo() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    // 권한 확인 및 요청 메소드
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    updateStorageInfo()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        } else {
            updateStorageInfo()
        }
    }

    // 저장소 정보를 업데이트하는 메소드
    private fun updateStorageInfo() {
        // 내부 저장소 정보 가져오기
        val internalStorageDir = Environment.getDataDirectory()
        val totalBytes = getTotalBytes(internalStorageDir)
        val freeBytes = getFreeBytes(internalStorageDir)
        val usedBytes = totalBytes - freeBytes

        // 사용량 퍼센트 계산
        val usedPercentage = if (totalBytes > 0) (usedBytes * 100) / totalBytes else 0

        storageData.value = StorageInfo(
            totalSize = formatSize(totalBytes),
            usedSize = formatSize(usedBytes),
            freeSize = formatSize(freeBytes),
            usedPercentage = usedPercentage.toFloat()
        )
    }

    // 전체 저장 용량을 반환하는 메소드
    private fun getTotalBytes(path: File): Long {
        val stats = StatFs(path.path)
        return stats.totalBytes
    }

    // 남은 저장 용량을 반환하는 메소드
    private fun getFreeBytes(path: File): Long {
        val stats = StatFs(path.path)
        return stats.availableBytes
    }

    // 바이트 단위를 사람이 읽기 쉬운 형식으로 변환하는 메소드
    private fun formatSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.##").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }
}

// 저장소 정보를 담는 데이터 클래스
data class StorageInfo(
    val totalSize: String,
    val usedSize: String,
    val freeSize: String,
    val usedPercentage: Float
)

@Composable
fun StorageInfoScreen(
    storageInfo: StorageInfo?,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "저장소 정보",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (storageInfo != null) {
                    InfoRow(label = "전체 용량:", value = storageInfo.totalSize)
                    InfoRow(label = "사용 중:", value = storageInfo.usedSize)
                    InfoRow(label = "남은 용량:", value = storageInfo.freeSize)
                    InfoRow(label = "사용률:", value = "${storageInfo.usedPercentage.toInt()}%")

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = storageInfo.usedPercentage / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                } else {
                    InfoRow(label = "전체 용량:", value = "계산 중...")
                    InfoRow(label = "사용 중:", value = "계산 중...")
                    InfoRow(label = "남은 용량:", value = "계산 중...")
                    InfoRow(label = "사용률:", value = "계산 중...")

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                }
            }
        }

        Button(
            onClick = onRefreshClick,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("새로고침")
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
fun StorageInfoPreview() {
    val sampleStorageInfo = StorageInfo(
        totalSize = "64 GB",
        usedSize = "32 GB",
        freeSize = "32 GB",
        usedPercentage = 50f
    )

    Istf_storageTheme {
        StorageInfoScreen(
            storageInfo = sampleStorageInfo,
            onRefreshClick = {}
        )
    }
}