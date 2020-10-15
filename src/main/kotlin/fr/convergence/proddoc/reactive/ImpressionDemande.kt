package fr.convergence.proddoc.reactive.fr.convergence.proddoc.kafka

import fr.convergence.proddoc.model.lib.obj.MaskMessage
import fr.convergence.proddoc.model.metier.DemandeImpression
import fr.convergence.proddoc.util.WSUtils
import fr.convergence.proddoc.util.stinger.StingerUtil
import io.vertx.core.logging.LoggerFactory
import org.eclipse.microprofile.reactive.messaging.Incoming
import java.io.InputStream
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.core.MediaType

@ApplicationScoped
class ImpressionDemande(@Inject val stingerUtil: StingerUtil) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ImpressionDemande::class.java)
    }
    /**
     * si un MaskMessage arrive sur le topic "IMPRESSION_DEMANDE" (Incoming) :
     * fait le "passe-plat" et appelle myGreffe pour lui demander d'imprimer le fichier
     * l'objet métier du message reçu est un "DemandeImpression"
     **/
    @Incoming("impression_demande")
    fun traiterEvenementImpressionDemande(messageIn: MaskMessage)  {

        //@TODO ces requires sont à basculer dans le maskIOHadler
        requireNotNull(messageIn.entete.typeDemande) { "message.entete.typeDemande est null" }
        requireNotNull(messageIn.objetMetier) { "message.objectMetier est null" }

        //récupérer dans le message, les valeurs des paramètres à passer à myGreffe
        val objetMessage = messageIn.recupererObjetMetier<DemandeImpression>()
        val IDSortieDocument =  objetMessage.IDsortieDocument
        val nbExemplaires =  objetMessage.nbExemplaires
        val nomBacEntree =  objetMessage.nomBacEntree
        val rectoVerso =  objetMessage.rectoVerso
        val nomImprimante =  objetMessage.nomImprimante
        val urlFichierAImprimer = objetMessage.urlFichierAImprimer

        LOG.debug("Demande d'impression reçue pour IDsortieDocument: $IDSortieDocument et fichier $urlFichierAImprimer")

        // appeler URI de myGreffe avec les paramètres nécessaires
        val reponseMyGreffe = WSUtils.demandeRestURLmyGreffe("/impression/demandeOK",
                mapOf(  "IDSortieDocument" to IDSortieDocument, "nbExemplaires" to nbExemplaires,
                        "nomBacEntree" to nomBacEntree,         "rectoVerso" to rectoVerso,
                        "nomImprimante" to nomImprimante,       "urlfichieraimprimer" to urlFichierAImprimer),
                5000,MediaType.MEDIA_TYPE_WILDCARD)
                .readEntity(String::class.java)
    }

}