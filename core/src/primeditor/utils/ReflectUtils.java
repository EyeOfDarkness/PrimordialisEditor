package primeditor.utils;

public class ReflectUtils{
    public static <T> Object get(Class<T> cs, T object, String field){
        try{
            var f = cs.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(object);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static <T> int getInt(Class<T> cs, T object, String field){
        try{
            var f = cs.getDeclaredField(field);
            f.setAccessible(true);
            return f.getInt(object);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
