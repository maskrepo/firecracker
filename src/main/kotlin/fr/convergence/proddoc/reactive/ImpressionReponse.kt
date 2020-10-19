package fr.convergence.proddoc.reactive

import fr.convergence.proddoc.model.lib.obj.MaskMessage
import fr.convergence.proddoc.model.metier.RetourImpression
import fr.convergence.proddoc.model.metier.RetourImpressionMyGreffe
import fr.convergence.proddoc.util.MaskIOHandler
import fr.convergence.proddoc.util.faireUneReponseAsynchrone
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.slf4j.LoggerFactory.getLogger
import javax.inject.Inject

class ImpressionReponse(@Inject var maskIOHandler: MaskIOHandler) {

    companion object {
        private val LOG = getLogger(ImpressionReponse::class.java)
    }

    @Inject
    @field: Channel("impression_reponse")
    var impressionReponseEmitter: Emitter<MaskMessage>? = null

    @Incoming("mygreffe_impression_reponse")
    fun traiterEvenementImpressionReponse(messageIn: MaskMessage) {

        //@TODO ces requires sont à basculer dans le maskIOHadler
        requireNotNull(messageIn.entete.typeDemande) { "message.entete.typeDemande est null" }
        requireNotNull(messageIn.objetMetier) { "message.objectMetier est null" }

        LOG.info("myGreffe a publié sur le topic la réponse suivante : ${messageIn}")

        faireUneReponseAsynchrone(impressionReponseEmitter) {
            try {
                //lire la réponse (c'est obligatoirement une réponse non-nulle au vu des requireNotNull du dessus)
                val statutImpression = messageIn.reponse!!.estReponseOk
                val urlFichierAImprimer = messageIn.recupererObjetMetier<RetourImpressionMyGreffe>().urlFichierAImprimer

                // envoyer message à Rhino
                if (statutImpression) {
                    MaskMessage.reponseOk(
                            RetourImpression("Impression de $urlFichierAImprimer OK"),
                            messageIn,
                            messageIn.entete.idReference)
                } else {
                    MaskMessage.reponseKo<Exception>(
                            IllegalStateException("Impression de $urlFichierAImprimer KO"),
                            messageIn,
                            messageIn.entete.idReference
                    )
                }

            } catch (ex: Exception) {
                MaskMessage.reponseKo<Exception>(ex, messageIn, messageIn.entete.idReference)
            }
        }
    }
}