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
- Adicione o arquivo **akatus_reader.xml** na pasta **assets** do seu projeto;
- Tenha uma Activity que implemente a interface **AkatusReaderInterface** e seus 3 métodos: <pre>handleAutoCfgStatus, handleReaderStatus e handleCardSwiped</pre>
- Instancie um objeto **AkatusReaderListener**;
- Inicie a **autoconfiguração** do leitor e aguarde até que o status **Conectado** seja retornado.

Exemplos:

```java
public class MainActivity extends Activity implements AkatusReaderInterface{

	@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.main);

			AkatusReaderListener reader = new AkatusReaderListener(this, this);
			if(reader.isConfigured())
				reader.connectWithProfile();
			else
				reader.startConfig();

	}

	@Override
	public void handleAutoCfgStatus(int status, String percent) {
		switch (status) {
		case AkatusReaderListener.AUTO_CFG_STARTED:
			// Exiba um ProgressDialog 'Aguarde'
			break;
		case AkatusReaderListener.AUTO_CFG_PROGRESS:
			// Atualize a mensagem com o parâmetro 'percent'
			break;
		case AkatusReaderListener.AUTO_CFG_FINISHED:
			// Um perfil válido foi encontrado e o método 'connectWithProfile' foi chamado implicitamente, aguarde o retorno com o status em 'handleReaderStatus'
			break;
		case AkatusReaderListener.AUTO_CFG_FAILED:
			// Não foi possível se conectar, tente outra vez
			break;
		}
	}

	@Override
	public void handleCardSwiped(byte[] data) {
	      //'data' representa os bytes criptografados do cartão, armazene-o para enviar junto ao objeto 'TransactionRequest'
	}

	@Override
	public void handleReaderStatus(int status) {
		switch (status) {
		case AkatusReaderListener.READER_CONNECTING:
			// O leitor está sendo reconhecido, aguarde
			break;
		case AkatusReaderListener.READER_CONNECTED_NO_PROFILE:
			// Conectou-se sem usar perfil de configuração, pode-se chamar 'reader.startCardReading()'
			break;
		case AkatusReaderListener.READER_CONNECTED:
			// A partir deste ponto pode-se chamar 'reader.startCardReading()'
			break;
		case AkatusReaderListener.READER_DISCONNECTED:
			// 'reader.startCardReading()' não surtirá efeito, conecte o leitor
			break;
		}
	}
}
```

### Métodos 'callback' da interface AkatusReaderInterface:

⁃ void handleAutoCfgStatus(int status, String percent): Utilizado para receber o andamento da autoconfiguração do leitor, sendo que, se esse processo não for concluído com sucesso, a aplicação não estará possibilitada de se conectar ao leitor.

As constantes definidas pela classe AkatusReaderListener para este método são descritas da seguinte forma:

**AUTO_CFG_STARTED:** Retornada quando a autoconfiguração é iniciada, através do método **startConfig()** da classe **AkatusReaderListener**, que será descrito mais adiante;

**AUTO_CFG_PROGRESS:** Retornada conforme novos **perfis de configuração** estão sendo utilizados para tentativa de configuração. É retornada também uma String contendo a porcentagem de perfis já utilizados;

**AUTO_CFG_FINISHED:** Retornada quando um perfil válido for encontrado no XML de configuração da Akatus, e uma chamada ao método **connectWithProfile()** da classe **AkatusReaderListener** será chamada implicitamente;

**AUTO_CFG_FAILED:** Retornada quando todos os perfis já foram utilizados e nenhum deles possibilitou a comunicação do dispositivo com o leitor.

Obs.: Caso a 1ª tentativa tenha este retorno, não considere que o dispositivo é incompatível com o leitor, pois já foi observado que, em alguns casos, uma nova tentativa de configuração (Liberando memória no aparelho) obteve sucesso.
