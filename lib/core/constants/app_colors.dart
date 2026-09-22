import 'package:flutter/material.dart';

/// Skema warna Kas Go yang tervalidasi aksesibilitasnya untuk visualisasi data dan tema.
class AppColors {
  AppColors._();

  // Primary & Surface
  static const Color primaryLight = Color(0xFF2A78D6); // Blue slot 1
  static const Color primaryDark = Color(0xFF3987E5);
  static const Color backgroundLight = Color(0xFFF8FAFC);
  static const Color backgroundDark = Color(0xFF1A1A19);
  static const Color surfaceLight = Color(0xFFFFFFFF);
  static const Color surfaceDark = Color(0xFF242423);

  // Text Tokens
  static const Color textPrimaryLight = Color(0xFF0F172A);
  static const Color textSecondaryLight = Color(0xFF64748B);
  static const Color textPrimaryDark = Color(0xFFF8FAFC);
  static const Color textSecondaryDark = Color(0xFF94A3B8);

  // Data Viz Categorical (Validated)
  static const Color incomeLight = Color(0xFF2A78D6); // Series 1 - Pemasukan
  static const Color incomeDark = Color(0xFF3987E5);

  static const Color expenseLight = Color(0xFFEB6834); // Series 2 - Pengeluaran
  static const Color expenseDark = Color(0xFFD95926);

  static const Color balanceLight = Color(0xFF1BAF7A); // Series 3 - Saldo
  static const Color balanceDark = Color(0xFF199E70);

  // Status Colors (Always paired with icon & label)
  static const Color statusPending = Color(0xFFFAB219);
  static const Color statusSuccess = Color(0xFF0CA30C);
  static const Color statusRejected = Color(0xFFD03B3B);
}
