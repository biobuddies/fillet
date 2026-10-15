package es.biobuddi.fillet;

class ProfileTest {

    public static void main(String[] arguments) {
        assert Profile.target("com.paris.velib", null, null).equals(
            "android-app://com.paris.velib"
        );
        assert Profile.target("com.android.chrome", null, "acparis.org").equals(
            "https://acparis.org"
        );
        assert Profile.target("org.mozilla.firefox", "http", "acparis.org").equals(
            "http://acparis.org"
        );
        var church = Profile.ALL.get(0);
        assert church.associates("android-app://com.google.android.apps.maps");
        assert church.associates("https://www.nps.gov");
        assert church.values().get(Field.POSTAL_CODE).equals("75007");
    }
}
