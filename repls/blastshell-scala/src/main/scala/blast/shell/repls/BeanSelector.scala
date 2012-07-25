package blast.shell.repls;

/**
 *
 *
 */
trait BeanSelector {
    
    def filter(beanName: String, clazz: Class[_], bean: Any): Boolean
    
}
