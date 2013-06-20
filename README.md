<p align="left" >
  <img src="https://site.akatus.com/wp-content/uploads/2012/12/logo.gif" alt="Akatus" title="Akatus">
</p>


#Akatus Mobile Android SDK

## Como integrar o Akatus Mobile em seu aplicativo Android

### I - Integrando o Leitor de Cartões:

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

#### Métodos 'callback' da interface AkatusReaderInterface:

⁃ **void handleAutoCfgStatus(int status, String percent)**: Utilizado para receber o andamento da autoconfiguração do leitor, sendo que, se esse processo não for concluído com sucesso, a aplicação não estará possibilitada de se conectar ao leitor.

As constantes definidas pela classe AkatusReaderListener para este método são descritas da seguinte forma:

```java AUTO_CFG_STARTED```:** Retornada quando a autoconfiguração é iniciada, através do método **startConfig()** da classe **AkatusReaderListener**, que será descrito mais adiante;

**AUTO_CFG_PROGRESS:** Retornada conforme novos **perfis de configuração** estão sendo utilizados para tentativa de configuração. É retornada também uma String contendo a porcentagem de perfis já utilizados;

**AUTO_CFG_FINISHED:** Retornada quando um perfil válido for encontrado no XML de configuração da Akatus, e uma chamada ao método **connectWithProfile()** da classe **AkatusReaderListener** será chamada implicitamente;

**AUTO_CFG_FAILED:** Retornada quando todos os perfis já foram utilizados e nenhum deles possibilitou a comunicação do dispositivo com o leitor.

Obs.: Caso a 1ª tentativa tenha este retorno, não considere que o dispositivo é incompatível com o leitor, pois já foi observado que, em alguns casos, uma nova tentativa de configuração (Liberando memória no aparelho) obteve sucesso.


- **void handleReaderStatus(int status)** : Utilizado para receber os estágios de conexão do dispositivo com o leitor de cartões. É retornado à partir da chamada ao método connectWithProfile() que será descrito posteriormente.

As constantes definidas pela classe AkatusReaderListener para este método são descritas da seguinte forma:

**READER_DISCONNECTED:** O leitor está desconectado ou a autoconfiguração ainda não foi concluída com sucesso;

**READER_CONNECTING:** O leitor foi conectado ao dispositivo e está em processo de reconhecimento. Ainda não é possível obter resultados de leitura;

**READER_CONNECTED:** O leitor está conectado e o dispositivo está preparado para enviar comandos como **startCardReading()** e **stopCardReading()**;

**READER_CONNECTED_NO_PROFILE:** O leitor está conectado, porém não foi utlizado um **perfil de configuração** como os outros. Um perfil especial foi encontrado, e para este tipo de dispositivo deve-se chamar o método **startConfig()** todas as vezes que quiser conectar-se ao leitor. É importante que sua aplicação armazene essa informação para futuras conexões.

**void handleCardSwiped(byte[] data):** Utilizado para receber os dados criptografados lidos do cartão. É retornado quando o cartão passar pelo leitor, após a chamada do método **startCardReading()** da classe **AkatusReaderListener**.

Obs.: Pode ser que a leitura não retorne os bytes corretamente, sendo assim, é recomendado utilizar a seguinte validação antes de enviar o array de bytes para o servidor da Akatus:

```java
try {
    String dataString = new String(data);
    String cardNo = dataString.substring(dataString.indexOf(";")+1, dataString.indexOf("="));
    // A varíavel cardNo agora representa o numero do cartão mascarado com '*'
} catch (IndexOutOfBoundsException e) {
    // Os dados não foram lidos corretamente, passe o cartão novamente
}
```


#### Métodos da classe AkatusReaderListener:

**void startConfig():** Utilizado para iniciar a autoconfiguração do leitor. A aplicação deve estar conectada à internet, caso contrário, não poderá se comunicar com o servidor da Akatus para saber qual configuração utilizar.

**void stopConfig():** Utilizado para interromper o processo de autoconfiguração, caso o usuário nao deseje esperar no momento. Obs.: Ao utilizar este método, a próxima chamada à  startConfig() iniciará as tentativas à partir do último perfil utilizado, evitando que retorne à '0%' ;

**boolean isConfigured():** Utilizado para saber se já existe um perfil de configuração salvo no banco de dados. Dispositivos que se conectam sem perfil **CONNECTED_NO_PROFILE** sempre retornarão **false**;

**void connectWithProfile():** Utilizado para conectar o dispositivo ao leitor usando um perfil de configuração salvo anteriormente. Terá efeito apenas se o método **isConfigured()** retornar **true**;

**void registerReader(boolean listen):** Utilizado para informar ao Listener se a Activity deve ou não ser informada com as atualizações de status do leitor. Exemplo: Activity A criou uma instância **AkatusReaderListener(this, this)**, e quando Activity A perder o foco, deve chamar esse método com o valor **false**, para que possíveis eventos de atualização não sejam iniciados para ela, causando erros de UI;

**void startCardReading():** Utilizado para informar ao leitor que deve iniciar o processo de leitura de cartões. Somente surtirá efeito após o método **handleReaderStatus** retornar **CONNECTED** ou **CONNECTED_NO_PROFILE**. Obs.: Este método deve ser chamado 1 vez para cada evento de leitura (retorno pelo **callback** handleCardSwiped);

**void stopCardReading():** Utilizado para informar ao leitor que não deve mais esperar pela leitura de cartões, geralmente chamado ao deixar a Acticity, seguido por **finalizeReader()**;

**void finalizeReader():** Encerra todos os processos relacionados ao leitor (banco de dados, perfis, evento de leitura). Deve ser chamado ao deixar a Activity (onDestroy).


### II – Enviando Transações para a o servidor Akatus:

Para começar a enviar transações para o servidor Akatus, são necessários os seguintes passos:

	⁃	Adicione gson-2.1.jar e httpmime-4.1.3.jar (ou equivalentes) ao seu projeto;
	⁃	Instancie um objeto 'TransactionRequest';
	⁃	Preencha os dados da transação
	⁃	Instancie um objeto **AkatusTransactionTemplate** e envie pelo método **postTransaction**, conforme o exemplo:

```java
private void sendAkatusTransaction(){
    TransactionRequest request = new TransactionRequest();

    request.setAmount("100.0");
    request.setCard_number("1234567891234567");
    request.setCvv("123");
    request.setDescription("ITEM1");
    request.setExpiration("201309"); // yyyyMM
    request.setGeolocation(new double[]{1234,1234}); // lat, longt
    request.setHolder_name("CLIENT");
    request.setInstallments("10"); // min: 1, max: 12
    request.setPhoto(photoByteArray);
    request.setSignature(signatureByteArray);
    request.setToken(authResponse.getToken());
    request.setTrack1(cardDataByteArray);

    try {
        AkatusTransactionTemplate transaction = new AkatusTransactionTemplate(true);
        TransactionResponse response = transaction.postTransaction(request);

        if(response.getReturn_code() == AkatusTransactionTemplate.TRANSACTION_OK)
            Toast.makeText(this, "Transaction accepted! " + response.getMessage(), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Transaction recused! " + response.getMessage(), Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
    }
}
```
