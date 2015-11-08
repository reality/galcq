
import java.util.*

/**
 * ALCQ Reasoner for OWLAPI
 */
class GalcqReasoner extends OWLReasonerBase {
  def classHierarchy = new ClassHierarchyInfo()
  def objectPropertyHierarchy = new ObjectPropertyHierarchyInfo()
  def dataPropertyHierarchy = new DataPropertyHierarchyInfo()

  static VERSION = new Version(0, 0, 0, 1)

  def progress
  def prepared = false
  def interrupted = false

  @Override
  public GalcqReasoner(ontology, configuration, bufferingMode) { 
    super(rootOntology, configuration, bufferingMode)
    checkNotNull(configuration, 'No null config')
    progress = configuration.getProgressMonitor()
    prepareReasoner()
  }

  public prepareReasoner() {
    classHierarchy.computeHierarchy()
    objectPropertyHierarchy.computeHierarchy()
    dataPropertyHierarchy.computeHierarchy()
    prepared = true
  }

  @Override
  public precomputeInferences(InferenceType... inferenceTypes) {
    prepareReasoner()
  }

  @Override
  public isPrecomputed(InferenceType inferenceType) {
    return prepared
  }

}
