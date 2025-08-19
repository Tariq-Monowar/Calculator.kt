package com.example.myapplication.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import java.util.Stack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }
        
        setContent {
            CalculatorTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CalculatorApp()
                }
            }
        }
    }
}

@Composable
fun CalculatorApp() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = input.ifEmpty { "0" },
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Light,
                    color = Color(0xFFA0A0A0)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.End,
                maxLines = 1
            )

            Text(
                text = result,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }

        val buttons = listOf(
            listOf("C", "⌫", "%", "/"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf(".", "0", "=")
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            buttons.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (rowIndex == buttons.size - 1) {

                        Button(
                            onClick = { input += "." },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .height(72.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2D2D2D),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                ".",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Button(
                            onClick = { input += "0" },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .height(72.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2D2D2D),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "0",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Button(
                            onClick = {
                                try {
                                    result = evaluateExpression(input.replace("×", "*")).toString()
                                } catch (e: Exception) {
                                    result = "Error"
                                }
                            },
                            modifier = Modifier
                                .weight(2f) // Takes double width
                                .height(80.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2D2D2D),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "=",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else {
                        row.forEach { button ->
                            Button(
                                onClick = {
                                    when (button) {
                                        "C" -> {
                                            input = ""
                                            result = "0"
                                        }
                                        "⌫" -> {
                                            if (input.isNotEmpty()) {
                                                input = input.dropLast(1)
                                            }
                                        }
                                        "%" -> {
                                            input += "/100"
                                        }
                                        else -> {
                                            input += button
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .height(72.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (button) {
                                        else -> Color(0xFF2D2D2D)
                                    },
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    button,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun evaluateExpression(expression: String): Double {
    if (expression.isEmpty()) return 0.0

    val tokens = expression.replace(" ", "").toCharArray()
    val values = Stack<Double>()
    val ops = Stack<Char>()

    var i = 0
    while (i < tokens.size) {
        when {
            tokens[i].isDigit() || tokens[i] == '.' -> {
                val sb = StringBuilder()
                while (i < tokens.size && (tokens[i].isDigit() || tokens[i] == '.')) {
                    sb.append(tokens[i++])
                }
                values.push(sb.toString().toDouble())
                i--
            }
            tokens[i] == '(' -> {
                ops.push(tokens[i])
            }
            tokens[i] == ')' -> {
                while (ops.peek() != '(') {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()))
                }
                ops.pop()
            }
            tokens[i] in "+-*/" -> {
                while (!ops.empty() && hasPrecedence(tokens[i], ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()))
                }
                ops.push(tokens[i])
            }
        }
        i++
    }

    while (!ops.empty()) {
        values.push(applyOp(ops.pop(), values.pop(), values.pop()))
    }

    return values.pop()
}

fun hasPrecedence(op1: Char, op2: Char): Boolean {
    if (op2 == '(' || op2 == ')') return false
    return (op2 == '*' || op2 == '/') && (op1 == '+' || op1 == '-')
}

fun applyOp(op: Char, b: Double, a: Double): Double {
    return when (op) {
        '+' -> a + b
        '-' -> a - b
        '*' -> a * b
        '/' -> {
            if (b == 0.0) throw UnsupportedOperationException("Cannot divide by zero")
            a / b
        }
        else -> 0.0
    }
}

@Composable
fun CalculatorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC5),
            background = Color(0xFF121212)
        ),
        typography = Typography(
            displayLarge = TextStyle(
                fontSize = 64.sp,
                lineHeight = 72.sp
            ),
            displayMedium = TextStyle(
                fontSize = 36.sp,
                lineHeight = 44.sp
            ),
            titleLarge = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        ),
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    CalculatorTheme {
        CalculatorApp()
    }
}