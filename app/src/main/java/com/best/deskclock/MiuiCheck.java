package com.best.deskclock;

import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
public class MiuiCheck {

    /**
     * Verifica si el dispositivo está ejecutando MIUI.
     *
     * De forma predeterminada, HyperOS está excluido de la verificación.
     * Si querés incluir HyperOS en la verificación, pasá excludeHyperOS como false.
     *
     * @param excludeHyperOS Indica si se debe excluir HyperOS.
     * @return True si el dispositivo está ejecutando MIUI, de lo contrario false.
     */
    public static boolean isMiui(boolean excludeHyperOS) {
        // Verificar si el dispositivo es de Xiaomi, Redmi o POCO.
        String brand = Build.BRAND.toLowerCase();
        Set<String> xiaomiBrands = new HashSet<>(Arrays.asList("xiaomi", "redmi", "poco"));
        if (!xiaomiBrands.contains(brand)) {
            return false;
        }

        // Esta propiedad está presente tanto en MIUI como en HyperOS.
        boolean isMiui = !isNullOrBlank(getProperty("ro.miui.ui.version.name"));
        // Esta propiedad es exclusiva de HyperOS y no está presente en MIUI.
        boolean isHyperOS = !isNullOrBlank(getProperty("ro.mi.os.version.name"));

        return isMiui && (!excludeHyperOS || !isHyperOS);
    }

    /**
     * Sobrecarga del método isMiui con excludeHyperOS como true por defecto.
     */
    public static boolean isMiui() {
        return isMiui(true);
    }

    // Función privada para obtener el valor de una propiedad del sistema.
    private static String getProperty(String property) {
        BufferedReader reader = null;
        try {
            Process process = Runtime.getRuntime().exec("getprop " + property);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Método auxiliar para verificar si una cadena es nula o está en blanco.
    private static boolean isNullOrBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
