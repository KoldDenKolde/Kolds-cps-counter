import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public final class RemapKoldPackages {
    private RemapKoldPackages() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Expected input stage dir and output stage dir");
        }

        Path input = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = Path.of(args[1]).toAbsolutePath().normalize();
        if (Files.exists(output)) {
            try (var paths = Files.walk(output)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        Files.createDirectories(output);

        try (var paths = Files.walk(input)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                try {
                    copyMapped(input, output, path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static void copyMapped(Path inputRoot, Path outputRoot, Path file) throws IOException {
        String relative = inputRoot.relativize(file).toString().replace('\\', '/');
        if (relative.endsWith(".class")) {
            byte[] bytes = remapClass(Files.readAllBytes(file));
            String name = new ClassReader(bytes).getClassName();
            Path output = outputRoot.resolve(name + ".class");
            Files.createDirectories(output.getParent());
            Files.write(output, bytes);
            return;
        }

        Path output = outputRoot.resolve(mapResourcePath(relative));
        Files.createDirectories(output.getParent());
        if (isText(relative)) {
            String text = Files.readString(file, StandardCharsets.UTF_8);
            Files.writeString(output, mapText(text), StandardCharsets.UTF_8);
        } else {
            Files.copy(file, output);
        }
    }

    private static byte[] remapClass(byte[] input) {
        ClassReader reader = new ClassReader(input);
        ClassWriter writer = new ClassWriter(0);
        reader.accept(new ClassRemapper(writer, new KoldRemapper()), ClassReader.SKIP_DEBUG);
        return writer.toByteArray();
    }

    private static boolean isText(String relative) {
        return relative.endsWith(".json")
            || relative.endsWith(".MF")
            || relative.startsWith("LICENSE")
            || relative.endsWith(".txt");
    }

    private static String mapResourcePath(String path) {
        String mapped = path;
        mapped = mapped.replace("com/oneaura/cpscounter/OneaurasCPSCounterClient", "com/kold/cpscounter/KoldsCPSCounterClient");
        mapped = mapped.replace("com/oneaura/cpscounter/OneaurasCPSCounter", "com/kold/cpscounter/KoldsCPSCounter");
        mapped = mapped.replace("com/oneaura/cpscounter/", "com/kold/cpscounter/");
        mapped = mapped.replace("com/oneauras/cpscounter/client/", "com/kold/cpscounter/client/");
        mapped = mapped.replace("com/oneaurasmini/", "com/kold/cpscounter/hitter/");
        return mapped;
    }

    private static String mapText(String text) {
        String mapped = text;
        mapped = mapped.replace("com.oneaura.cpscounter.OneaurasCPSCounterClient", "com.kold.cpscounter.KoldsCPSCounterClient");
        mapped = mapped.replace("com.oneaura.cpscounter.OneaurasCPSCounter", "com.kold.cpscounter.KoldsCPSCounter");
        mapped = mapped.replace("com.oneaura.cpscounter.", "com.kold.cpscounter.");
        mapped = mapped.replace("com.oneauras.cpscounter.client.", "com.kold.cpscounter.client.");
        mapped = mapped.replace("com.oneaurasmini.", "com.kold.cpscounter.hitter.");
        mapped = mapped.replace("com/oneaura/cpscounter/OneaurasCPSCounterClient", "com/kold/cpscounter/KoldsCPSCounterClient");
        mapped = mapped.replace("com/oneaura/cpscounter/OneaurasCPSCounter", "com/kold/cpscounter/KoldsCPSCounter");
        mapped = mapped.replace("com/oneaura/cpscounter/", "com/kold/cpscounter/");
        mapped = mapped.replace("com/oneauras/cpscounter/client/", "com/kold/cpscounter/client/");
        mapped = mapped.replace("com/oneaurasmini/", "com/kold/cpscounter/hitter/");
        mapped = mapped.replace("oneaurasmini", "kolds-cps-counter");
        return mapped;
    }

    private static final class KoldRemapper extends Remapper {
        @Override
        public String mapFieldName(String owner, String name, String descriptor) {
            return mapMemberName(name);
        }

        @Override
        public String mapMethodName(String owner, String name, String descriptor) {
            return mapMemberName(name);
        }

        @Override
        public String map(String internalName) {
            if (internalName == null) {
                return null;
            }
            if (internalName.equals("com/oneaura/cpscounter/OneaurasCPSCounterClient")) {
                return "com/kold/cpscounter/KoldsCPSCounterClient";
            }
            if (internalName.startsWith("com/oneaura/cpscounter/OneaurasCPSCounterClient$")) {
                return "com/kold/cpscounter/KoldsCPSCounterClient$"
                    + internalName.substring("com/oneaura/cpscounter/OneaurasCPSCounterClient$".length());
            }
            if (internalName.equals("com/oneaura/cpscounter/OneaurasCPSCounter")) {
                return "com/kold/cpscounter/KoldsCPSCounter";
            }
            if (internalName.startsWith("com/oneaura/cpscounter/")) {
                return "com/kold/cpscounter/" + internalName.substring("com/oneaura/cpscounter/".length());
            }
            if (internalName.startsWith("com/oneauras/cpscounter/client/")) {
                return "com/kold/cpscounter/client/" + internalName.substring("com/oneauras/cpscounter/client/".length());
            }
            if (internalName.startsWith("com/oneaurasmini/")) {
                return "com/kold/cpscounter/hitter/" + internalName.substring("com/oneaurasmini/".length());
            }
            return internalName;
        }

        private static String mapMemberName(String name) {
            if (name == null) {
                return null;
            }
            return name
                .replace("oneaurasmini", "kold")
                .replace("oneauras", "kold")
                .replace("oneaura", "kold");
        }
    }
}
