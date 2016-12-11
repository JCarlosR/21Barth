Versi�n inicial (offline) (1 jugador y dealer)
---
La pantalla se encontraba repartida como lo muestra la imagen siguiente:

En este caso, en la parte superior izquierda se encuentra el dealer y su score representado por un signo de interrogaci�n.

Debajo de las cartas del dealer se encuentra la apuesta realizada por el jugador. 
El jugador ganar� o perder� esa cantidad.

Centrado verticalmente hacia la derecha se encuentra el balance del jugador. 
Si llega a 0, pierde.

En el lado inferior izquierdo se encuentran las cartas del jugador.
Y un poco m�s a la derecha, su score actual.

Finalmente, en la parte inferior se encuentran las opciones del juego.


Versi�n realtime (online) (2 jugadores y dealer)
---
Los elementos visuales se han reorganizado con la finalidad de que quepan los nuevos componentes asociados al segundo jugador.

Imaginemos que la pantalla es dividida en 4 cuadrantes del mismo tama�o, pero siempre reservando un espacio inferior para las opciones del juego. 

En la secci�n superior izquierda se encuentran las cartas del dealer y su score.
En la secci�n superior derecha el cuadrante presenta un espacio reservado para cuando el juego soporte 3 participantes.

En la secci�n inferior izquierda se encuentran las cartas del jugador 1, su score actual, su balance y apuesta actual.
En la secci�n inferior derecha se encuentran las cartas del jugador 2, su score actual, su balance y apuesta actual.

Cambios en la UI
---
La descripci�n anterior permite tener una idea general de los cambios realizados. A continuaci�n se detalla un poco en t�rminos de Android:

1. La vista se representaba mediante posiciones relativas usando un RelativeLayout. Ahora se usar�n LinearLayouts con su correspondente orientaci�n vertical u horizontal para ubicar los elementos.
2. La vista presentaba LinearLayouts con 1 �nico elemento hijo. Estos contenedores se han reemplazado por FrameLayouts por ser m�s id�neos como contenedores de 1 �nico elemento.

Por �ltimo las opciones ubicadas en la parte inferior, van mostrando el valor que se pretende apostar, y permiten finalmente realizar una apuesta.
Luego que se han fijado las apuestas, los valores correspondientes se muestran en cada cuadrante, con relaci�n a cada participante.

Flujo del juego para 1 solo participante (offline)
---
1. El participante determina la cantidad a apostar y presiona el bot�n DEAL.
2. Las cartas son repartidas al dealer y al �nico participante en un instante. Por tanto el participante puede ver la suma de las cartas que tiene.
3. El participante debe usar HIT (para robar una carta adicional) o STAND (para finalizar su turno). Solo si el participante obtuviese el valor de 21 desde el inicio, gana autom�ticamente sin hacer nada m�s (BLACKJACK).
3.1. Si el participante decide robar una carta y su suma excede 21, pierde por quiebre (BUSTED).
3.2. Si el participante decide finalizar su turno, se muestra la suma del DEALER y seg�n ello se determina el deselnace (el dealer puede perder por quiebre, puede ocurrir un empate o DRAW, o bien ganar quien tenga el valor m�s cercano a 21 o 21).
4. El participante repetir� su apuesta dando inicio a un segundo round, y podr� hacer esto mientras disponga de un balance mayor a 0. Por ahora el juego soporta apuestas que siempre son m�ltiplos de 10.

Flujo del juego para 2 participantes (online) (flujo simplificado)
---
Para esta **primera versi�n** no se van a considerar varios detalles, a fin de desarrollar una primera funcionalidad b�sica.
La idea es que 2 jugadores puedan compartir la misma mesa y jueguen contra el dealer, con sus apuestas y balances correspondientes.

1. Los jugadores definen sus apuestas.
Aqu� los jugadores determinan con sus opciones la cantidad que van a apostar y presionan el bot�n DEAL.
Cuando el primer jugador presione DEAL la cantidad que ha apostado se mostrar� en pantalla, y tambi�n lo ver� el otro jugador.
Este primer paso finaliza en el instante que ambos han definido sus apuestas. 
**De momento, no existe un tiempo l�mite** para decidir si apostar o no, por lo que si un jugador no reacciona el juego no avanzar� a un 2do paso, ni el jugador ser� expulsado (estas medidas se implementar�n luego).

2. Mostar cartas.
En este instante, las cartas que obtuvieron los jugadores son mostradas, y cada jugador puede ver las cartas que tiene adem�s de las del otro participante. No hay ning�n problema porque ambos est�n jugando contra el dealer de forma independiente.

 - Si uno de los jugadores (podr�a aplicar incluso para ambos) ha obtenido BLACKJACK este paso a terminado para �l. Cuando el paso finalice para ambos, reci�n se podr� ir a un tercer paso. Al igual que el caso anterior, no hay tiempo l�mite para actuar.
 - Cada jugador debe decidir entre las opciones HIT y STAND. La opci�n STAND finaliza este segundo paso para el jugador que ha decidido usar STAND. La opci�n de HIT finaliza el turno si el jugador excede 21 o si obtiene 21; de caso contrario, se puede usar HIT o STAND repitiendo lo antes mencionado.
 - El jugador que finalice su turno primero ver� sus resultados de forma inmediata, pero no podr� continuar hasta que el otro jugador finalice.

3. Resultados.
Cuando finalice la acci�n de cada jugador seg�n el punto anterior, se mostrar�n los resultados para ambos jugadores.
Es decir, ambos ver�n los resultados del jugador 1 y del jugador 2.
En este punto, se solicitar� que realicen click sobre el mensaje que muestra los resultados, esto confirmar� que est�n listos para una segunda ronda, que inicia por volver a definir una apuesta.
Cuando ambos hayan confirmado se repetir� lo descrito en (1). Sin embargo, si uno de ellos llega a tener un balance de 0, el juego terminar�a para ambos. Esto es para evitar definir por ahora una l�gica que permita a uno jugar y al otro solo observar.

* Por ahora, la l�gica que soporta este flujo es la entidad GameOnline, que se ha basado en la entidad Game, pero que soporta 2 jugadores y las consideraciones para jugar de forma online. Los jugadores por ahora son atributos de tipo Player. Se espera en versiones posteriores, reemplazar estos 2 objetos por un ArrayList que soporte una mayor cantidad de participantes en simult�neo.

Broadcast
---
El juego ButtonClicker2000 (que Google ha publicado a modo de ejemplo) consiste en que cada jugador hace broadcast (emite mensajes) a los dem�s jugadores, informando por cada click que ha realizado.

Estos mensajes se emiten a trav�s de un buffer (determinado por un arreglo de bytes), y en el caso del juego mencionado existen 2 tipos de mensajes. 
Un primer tipo comunica a los dem�s jugadores el valor del score del jugador que emite el mensaje, y esto ocurre cada vez que se hace click durante los 20 segundos que dura el juego.
Un segundo tipo, tambi�n comunica el score del jugador, pero es un score que se emite solo una vez al final del juego, cunado el tiempo se ha agotado.
En este caso, el buffer consta de 2 bytes, el primero indica el tipo de mensaje a trav�s de una letra y el segundo byte es el score del jugador.

En el caso de 21Barth, debemos manejar m�s tipos de mensajes. Estos mensajes comunicar�n al otro jugador la acci�n que cada uno de ellos realiza. As� tenemos:

- Definir apuesta. Indica que el jugador ha presionado el bot�n de DEAL luego de definir el valor de su apuesta. En este caso el array de bytes constar� de 2 bytes. El primer byte ser� la letra "D" y el segundo byte contendr� el valor que se ha decidido apostar.

- Solicitar carta. Indica que el jugador ha presionado el bot�n HIT. En este caso el host va a generar una carta y har� broadcast de este valor al jugador que presion� HIT. Solicitar 1 carta se representa por 2 bytes: el caracter H y el n�mero 0.
Mientras que asignar una carta se representa por 3 bytes: el caracter H, un caracter que representa un suite (HEARTS, DIAMONDS, CLUBS, SPADES) y un value (representado por un caracter, por ejemplo "A", "Q", "1").

- Finalizar jugada. Indica que el jugador ha presionado el bot�n STAND. En este caso el buffer a comunicar consta solo de 1 caracter, determinado por la letra S.

- Repartir mano inicial. Este mensaje lo emite el host de la partida y se usa para comunicar las 2 primeras cartas al jugador que no es host. El formato del mensaje es I, seguido de un suite y un value correspondientes a la primera carta, y luego nuevamente un suite y value (segunda carta).
