<p align="left" >
  <img src="https://site.akatus.com/wp-content/uploads/2012/12/logo.gif" alt="Akatus" title="Akatus">
</p>


#Akatus Mobile Android SDK

### Como integrar o Akatus Mobile em seu aplicativo Android

I - Integrando o Leitor de Cartões:

Para que seu aplicativo esteja apto à receber informações vindas do Leitor de Cartões Akatus, os seguintes passos serão necessários:

- Defina as seguintes permissões no seu arquivo AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

- Adicione **AkatusLib.jar** ao seu projeto;
- Adicione **IDTuniMagSDKAndroid.jar** ao seu projeto;
- Adicione o arquivo **akatus_reader.xml** na pasta 'assets' do seu projeto;
- Tenha uma Activity que implemente a interface **AkatusReaderInterface** e seus 3 métodos: <pre>handleAutoCfgStatus, handleReaderStatus e handleCardSwiped</pre>
- Instancie um objeto **AkatusReaderListener**;
- Inicie a **autoconfiguração** do leitor e aguarde até que o status **Conectado** seja retornado.
