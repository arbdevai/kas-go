import 'package:flutter/material.dart';

/// Skema warna Kas Go 2026 — White Theme dengan aksen Royal Amethyst (Ungu Premium).
class AppColors {
  AppColors._();

  // Background & Surfaces
  static const Color backgroundLight = Color(0xFFFAFAFC); // Warm porcelain ground
  static const Color surfaceLight = Color(0xFFFFFFFF);
  static const Color surfaceMuted = Color(0xFFF8F7FC);
  static const Color surfaceLavender = Color(0xFFF5F3FF); // Soft lavender for icon backgrounds
  static const Color borderSubtle = Color(0xFFEDE9FE); // Subtle purple-tinted divider

  // Dark Mode Surfaces
  static const Color backgroundDark = Color(0xFF0F0B1E);
  static const Color surfaceDark = Color(0xFF191330);
  static const Color borderDark = Color(0xFF2E2452);

  // Royal Amethyst Palette (Brand Dominan)
  static const Color primaryRoyal = Color(0xFF5B21B6); // Deep Royal Amethyst
  static const Color primarySoft = Color(0xFF7C3AED); // Soft Iris
  static const Color primaryDark = Color(0xFF4C1D95);
  static const Color heroGradientStart = Color(0xFF431407); // Subtle rich accent
  static const Color heroPurpleStart = Color(0xFF4C1D95);
  static const Color heroPurpleEnd = Color(0xFF6D28D9);

  // Text Tokens
  static const Color textPrimaryLight = Color(0xFF0F172A); // Deep Navy Slate
  static const Color textSecondaryLight = Color(0xFF64748B); // Slate
  static const Color textMutedLight = Color(0xFF94A3B8);
  static const Color textOnPurple = Color(0xFFFFFFFF);
  static const Color textOnPurpleMuted = Color(0xFFDDD6FE);

  static const Color textPrimaryDark = Color(0xFFF8FAFC);
  static const Color textSecondaryDark = Color(0xFF94A3B8);

  // Luxury Accent
  static const Color accentGold = Color(0xFFF59E0B);
  static const Color accentGoldLight = Color(0xFFFEF3C7);

  // Financial Status Tokens (Validated Contrast & CVD Safe)
  static const Color incomeGreen = Color(0xFF059669); // Emerald
  static const Color incomeGreenBg = Color(0xFFECFDF5);

  static const Color expenseRed = Color(0xFFDC2626); // Crimson
  static const Color expenseRedBg = Color(0xFFFEF2F2);

  static const Color balanceBlue = Color(0xFF2563EB);

  // Data Viz Categorical (Matching Dataviz Palette)
  static const Color vizIncome = Color(0xFF059669);
  static const Color vizExpense = Color(0xFFDC2626);
  static const Color vizBalance = Color(0xFF7C3AED);
}
