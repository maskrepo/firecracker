package fr.convergence.proddoc.reactive

import fr.convergence.proddoc.model.lib.obj.MaskMessage
import fr.convergence.proddoc.model.metier.RetourImpressionMyGreffe
import io.vertx.core.logging.LoggerFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eclipse.microprofile.reactive.messaging.Incoming
import javax.inject.Inject

class ImpressionReponse {

    companion object {
        private val LOG = LoggerFactory.getLogger(ImpressionReponse::class.java)
    }

    @Inject
    @field: Channel("impression_reponse")
    var retourEmitter: Emitter<MaskMessage>? = null

    @Incoming("mygreffe_impression_reponse")
    fun traiterEvenementImpressionReponse(messageIn: MaskMessage) {

        //@TODO ces requires sont à basculer dans le maskIOHadler
        requireNotNull(messageIn.entete.typeDemande) { "message.entete.typeDemande est null" }
        requireNotNull(messageIn.objetMetier) { "message.objectMetier est null" }

        LOG.info("myGreffe a publié sur le topic la réponse suivante : ${messageIn}")

        var messageOut = messageIn
        GlobalScope.launch {
            try {
                //lire la réponse (c'est obligatoirement une réponse non-nulle au vu des requireNotNull du dessus)
                val statutImpression = messageIn.reponse!!.estReponseOk
                val urlFichierAImprimer = messageIn.recupererObjetMetier<RetourImpressionMyGreffe>().urlFichierAImprimer

                // envoyer message à Rhino
                if (statutImpression) {
                    messageOut = MaskMessage.reponseOk(fr.convergence.proddoc.model.metier.RetourImpression("Impression de $urlFichierAImprimer OK"),
                                                        messageIn, messageIn.entete.idReference)
                } else {
                    messageOut = MaskMessage.reponseKo<Exception>(
                        IllegalStateException("Impression de $urlFichierAImprimer KO"),
                        messageIn,
                        messageIn.entete.idReference
                    )
                }

            } catch (ex: Exception) {
                messageOut = MaskMessage.reponseKo<Exception>(ex, messageIn, messageIn.entete.idReference)
            } finally {
                retour(messageOut)
            }
        }
    }

//    private suspend fun retour(message: MaskMessage) {
        private  fun retour(message: MaskMessage) {
        LOG.info("Reponse asynchrone = $message")
        retourEmitter?.send(message)
    }

}