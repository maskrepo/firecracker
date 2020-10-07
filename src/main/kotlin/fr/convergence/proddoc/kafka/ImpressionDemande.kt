package fr.convergence.proddoc.kafka.fr.convergence.proddoc.kafka

import fr.convergence.proddoc.model.lib.obj.MaskMessage
import fr.convergence.proddoc.model.metier.DemandeImpression
import fr.convergence.proddoc.util.WSUtils
import fr.convergence.proddoc.util.stinger.StingerUtil
import io.vertx.core.logging.LoggerFactory
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Outgoing
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
     * fait le "passe-plat" et appelle myGreffe pour lui demander d'imprimet le fichier
     * l'objet métier du message reçu est un "FichierStocke"
     **/
    @Incoming("impression_demande")
    fun traiterEvenementImpressionDemande(messageIn: MaskMessage)  {

        //@TODO ces requires sont à basculer dans le maskIOHadler
        requireNotNull(messageIn.entete.typeDemande) { "message.entete.typeDemande est null" }
        requireNotNull(messageIn.objetMetier) { "message.objectMetier est null" }

        val urlFichierAImprimer = messageIn.recupererObjetMetier<DemandeImpression>().urlFichierAImprimer
        val urlCallback = "http://${stingerUtil.host}:${stingerUtil.port}/reponseimpression"
        LOG.debug("Demande d'impression reçue pour : $urlFichierAImprimer")

        // Construire URI de myGreffe
        val uriCible = WSUtils.fabriqueURI(
                "/impressiondemande", WSUtils.TypeRetourWS.HTTP_RESPONSE,
                mapOf("urlfichieraimprimer" to urlFichierAImprimer, "urlcallback" to urlCallback)
        )

        // Appeler myGreffe
        val reponseMyGreffe = WSUtils.appelleURI(uriCible, 10000, MediaType.MEDIA_TYPE_WILDCARD).readEntity(String::class.java)

    }

}